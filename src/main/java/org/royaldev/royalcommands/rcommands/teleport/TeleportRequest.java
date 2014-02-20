package org.royaldev.royalcommands.rcommands.teleport;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeleportRequest {

    private final static Map<String, List<TeleportRequest>> teleportRequests = new HashMap<String, List<TeleportRequest>>();
    private final String requester;
    private final String target;
    private final TeleportType teleportType;

    public TeleportRequest(String requester, String target, TeleportType teleportType) {
        this.requester = requester;
        this.target = target;
        this.teleportType = teleportType;
    }

    public static Map<String, List<TeleportRequest>> getRequests() {
        return TeleportRequest.teleportRequests;
    }

    public static void send(Player requester, Player target, TeleportType teleportType) {
        TeleportRequest.send(requester, target, teleportType, true);
    }

    public static void send(Player requester, Player target, TeleportType teleportType, boolean confirmation) {
        if (requester.getName().equalsIgnoreCase(target.getName())) {
            requester.sendMessage(MessageColor.NEGATIVE + "You cannot teleport to yourself.");
            return;
        }
        final TeleportRequest tr = new TeleportRequest(requester.getName(), target.getName(), teleportType);
        List<TeleportRequest> trs;
        synchronized (teleportRequests) {
            trs = teleportRequests.get(target.getName());
        }
        if (trs == null) trs = new ArrayList<TeleportRequest>();
        if (TeleportRequest.hasPendingRequest(requester.getName(), target.getName())) {
            requester.sendMessage(MessageColor.NEGATIVE + "You already have a request pending with " + MessageColor.NEUTRAL + target.getName() + MessageColor.NEGATIVE + ".");
            return;
        }
        trs.add(tr);
        synchronized (teleportRequests) {
            teleportRequests.put(target.getName(), trs);
        }
        target.sendMessage(teleportType.getMessage(requester));
        target.sendMessage(MessageColor.POSITIVE + "To accept, use " + MessageColor.NEUTRAL + "/tpaccept" + MessageColor.POSITIVE + ". To decline, use " + MessageColor.NEUTRAL + "/tpdeny" + MessageColor.POSITIVE + ".");
        if (confirmation)
            requester.sendMessage(MessageColor.POSITIVE + "Request sent to " + MessageColor.NEUTRAL + target.getName() + MessageColor.POSITIVE + ".");
    }

    /**
     * Checks to see if a request with the given requester and target names exists, regardless of the request type.
     *
     * @param requester Requester's name
     * @param target    Target's name
     * @return true if requests exist, false if not
     */
    private static boolean hasPendingRequest(String requester, String target) {
        final List<TeleportRequest> trs = TeleportRequest.getRequests().get(target);
        if (trs == null) return false;
        for (TeleportRequest tr : trs) {
            if (tr == null || !tr.getRequester().equalsIgnoreCase(requester) || !tr.getTarget().equalsIgnoreCase(target))
                continue;
            return true;
        }
        return false;
    }

    /**
     * Gets the first registered TeleportRequest with the given requester and target names.
     *
     * @param requester Requester's name
     * @param target    Target's name
     * @return First matching TeleportRequest or null
     */
    public static TeleportRequest getFirstRequest(String requester, String target) {
        final List<TeleportRequest> trs = TeleportRequest.getRequests().get(target);
        if (trs == null) return null;
        for (TeleportRequest tr : trs) {
            if (tr == null || !tr.getRequester().equalsIgnoreCase(requester) || !tr.getTarget().equalsIgnoreCase(target))
                continue;
            return tr;
        }
        return null;
    }

    public String getRequester() {
        return requester;
    }

    public String getTarget() {
        return target;
    }

    public TeleportType getType() {
        return teleportType;
    }

    /**
     * Causes this TeleportRequest to expire, removing it from the registered list of requests.
     */
    public void expire() {
        final List<TeleportRequest> trs = TeleportRequest.getRequests().get(this.getTarget());
        if (trs == null) return; // not registered, then
        trs.remove(this);
        TeleportRequest.getRequests().put(this.getRequester(), trs);
    }

    /**
     * Accepts this TeleportRequest and attempts the teleport. Also sends confirmation messages to both parties.
     *
     * @return true if a teleport was attempted, false if not
     */
    public void accept() {
        this.expire();
        final Player requester = Bukkit.getPlayerExact(this.getRequester());
        final Player target = Bukkit.getPlayerExact(this.getTarget());
        if (requester == null || target == null) return;
        String error;
        switch (this.getType()) {
            case TO:
                error = RUtils.teleport(requester, target);
                break;
            case HERE:
                error = RUtils.teleport(target, requester);
                break;
            default:
                return;
        }
        final boolean success = error.isEmpty();
        final String message = MessageColor.POSITIVE + "The request to teleport " + MessageColor.NEUTRAL + ((this.getType() == TeleportType.TO) ? requester.getName() : target.getName()) + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + ((this.getType() == TeleportType.TO) ? target.getName() : requester.getName()) + MessageColor.POSITIVE + " was accepted.";
        if (success) {
            requester.sendMessage(message + " accepted.");
            target.sendMessage(message);
        } else {
            final String errorMessage = message.substring(0, message.length() - 1).replace(MessageColor.POSITIVE.toString(), MessageColor.NEGATIVE.toString()) + ", but there was an error.";
            requester.sendMessage(errorMessage);
            requester.sendMessage(MessageColor.NEGATIVE + error);
            target.sendMessage(errorMessage);
            target.sendMessage(MessageColor.NEGATIVE + error);
        }
    }

    /**
     * Denies the teleport request and {@link #expire()}s it. Also sends confirmation messages to both parties.
     */
    public void deny() {
        this.expire();
        final Player requester = Bukkit.getPlayerExact(this.getRequester());
        final Player target = Bukkit.getPlayerExact(this.getTarget());
        final String message = MessageColor.POSITIVE + "The request to teleport " + MessageColor.NEUTRAL + ((this.getType() == TeleportType.TO) ? requester.getName() : target.getName()) + MessageColor.POSITIVE + " to " + MessageColor.NEUTRAL + ((this.getType() == TeleportType.TO) ? target.getName() : requester.getName()) + MessageColor.POSITIVE + " was denied.";
        if (requester != null)
            requester.sendMessage(message.replace(MessageColor.POSITIVE.toString(), MessageColor.NEGATIVE.toString()));
        if (target != null) target.sendMessage(message);
    }

    public enum TeleportType {
        /**
         * Teleporting the requester to the target.
         */
        TO(MessageColor.NEUTRAL + "%s" + MessageColor.POSITIVE + " would like to teleport to you."),
        /**
         * Teleporting the target to the requester.
         */
        HERE(MessageColor.NEUTRAL + "%s" + MessageColor.POSITIVE + " would like you to teleport to them.");

        private final String requestMessage;

        TeleportType(String requestMessage) {
            this.requestMessage = requestMessage;
        }

        /**
         * Gets the message to send to the target. Must be formatted using {@link java.lang.String#format} to insert the
         * requester's name.
         *
         * @return Unformatted message
         */
        public String getRequestMessage() {
            return this.requestMessage;
        }

        /**
         * Formats the output of {@link #getRequestMessage()} with the given name.
         *
         * @param name Name to insert into the message
         * @return Formatted message
         */
        public String getMessage(String name) {
            return String.format(this.requestMessage, name);
        }

        /**
         * Formats the output of {@link #getRequestMessage()} with the name of the given CommandSender.
         *
         * @param cs CommandSender to get name from to insert into the message
         * @return Formatted message
         */
        public String getMessage(CommandSender cs) {
            return this.getMessage(cs.getName());
        }
    }
}
