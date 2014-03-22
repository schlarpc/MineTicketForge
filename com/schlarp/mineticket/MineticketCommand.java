package com.schlarp.mineticket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import argo.jdom.JsonRootNode;

import com.google.common.base.Joiner;

public class MineticketCommand implements ICommand {

	private List aliases;
	private static final Joiner joiner = Joiner.on(" ").skipNulls();

	public MineticketCommand() {
		this.aliases = new ArrayList();
		this.aliases.add("mineticket");
	}

	@Override
	public String getCommandName() {
		return "mineticket";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/mineticket <command>";
	}

	@Override
	public List getCommandAliases() {
		return this.aliases;
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender icommandsender) {
		return true;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender icommandsender, String[] astring) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] astring, int i) {
		return false;
	}

	@Override
	public int compareTo(Object o) {
		return 0;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] astring) {
		if (!(sender instanceof EntityPlayer)) {
			respondToUser(sender, "Only available to players.");
			return;
		}
		
		if (astring.length == 0) {
			respondToUser(sender, "Usage: /mineticket <command> <arguments ...>");
			return;
		}

		JsonRootNode response = null;

		if (astring[0].equals("activate")) {
			if (astring.length != 2) {
				respondToUser(sender, "Invalid arguments. Usage: /mineticket activate <email>");
				return;
			}
			response = MineticketAPI.activateUser(sender.getCommandSenderName(), astring[1]);
			if (response.getStringValue("status").equals("success")) {
				respondToUser(sender, "Your account has been activated.");
			}

		} else if (astring[0].equals("ban")) {
			if (astring.length < 2) {
				respondToUser(sender,
						"Invalid arguments. Usage: /mineticket ban <player> <optional reason ...>");
				return;
			}

			String reason = "";
			if (astring.length > 2) {
				reason = joiner.join(Arrays.copyOfRange(astring, 2, astring.length));
			}

			response = MineticketAPI.banUser(astring[1], sender.getCommandSenderName(), "ban",
					reason);
			if (response.getStringValue("status").equals("success")) {
				respondToUser(sender, "The player has been banned.");
			}

		} else if (astring[0].equals("unban")) {
			if (astring.length != 2) {
				respondToUser(sender, "Invalid arguments. Usage: /mineticket unban <player>");
				return;
			}

			response = MineticketAPI.banUser(astring[1], sender.getCommandSenderName(), "unban",
					null);
			if (response.getStringValue("status").equals("success")) {
				respondToUser(sender, "The player has been unbanned.");
			}

		} else {
			response = MineticketAPI.unknownCommand(sender.getCommandSenderName(), joiner.join(astring));
			if (response.getStringValue("status").equals("success")) {
				respondToUser(sender, response.getStringValue("message"));
			}

		}

		if (response.getStringValue("status").equals("failed")) {
			if (response.isStringValue("message")) {
				respondToUser(sender, response.getStringValue("message"));
			} else {
				respondToUser(sender, "Your request to the server failed.");
			}
		}

	}

	public static void respondToUser(ICommandSender user, String message) {
		user.sendChatToPlayer(ChatMessageComponent.createFromText("[MineTicket] " + message));
	}

}