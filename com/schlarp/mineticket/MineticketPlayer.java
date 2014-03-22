package com.schlarp.mineticket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import argo.jdom.JsonRootNode;
import cpw.mods.fml.common.IPlayerTracker;

public class MineticketPlayer implements IPlayerTracker {
	@Override
	public void onPlayerLogout(EntityPlayer player) {
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
	}

	@Override
	public void onPlayerLogin(EntityPlayer player) {
		JsonRootNode response = MineticketAPI.registerUser(player.getCommandSenderName());

		if (response.isStringValue("account_status")) {
			if (response.getStringValue("account_status").equals("unactivated")) {
				ChatMessageComponent chat = ChatMessageComponent
						.createFromText("[MineTicket] "
								+ "Your account is unactivated. "
								+ "You can activate by using the \"/mineticket activate <email>\" command, "
								+ "or by using the following activation key on the MineTicket website: "
								+ response.getStringValue("activation_key"));

				player.sendChatToPlayer(chat);
			}
		}
	}
}
