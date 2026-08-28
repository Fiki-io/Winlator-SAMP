package com.winlator.samp;

import android.content.Context;

import com.winlator.inputcontrols.Binding;
import com.winlator.inputcontrols.ControlElement;
import com.winlator.inputcontrols.ControlsProfile;
import com.winlator.inputcontrols.InputControlsManager;

public class GtaControlsProfile {
    public static final String PROFILE_NAME = "GTA SA-MP Default";

    public static ControlsProfile getOrCreateProfile(Context context) {
        InputControlsManager manager = new InputControlsManager(context);
        for (ControlsProfile profile : manager.getProfiles()) {
            if (PROFILE_NAME.equals(profile.getName())) {
                return profile;
            }
        }

        ControlsProfile profile = manager.createProfile(PROFILE_NAME);
        profile.setCursorSpeed(1.2f);

        // 1. Left Analog Stick (WASD)
        ControlElement leftStick = new ControlElement(null);
        leftStick.setType(ControlElement.Type.STICK);
        leftStick.setBindingAt(0, Binding.KEY_W); // Up
        leftStick.setBindingAt(1, Binding.KEY_D); // Right
        leftStick.setBindingAt(2, Binding.KEY_S); // Down
        leftStick.setBindingAt(3, Binding.KEY_A); // Left
        leftStick.setX((short) 130);
        leftStick.setY((short) 540);
        leftStick.setScale(1.4f);
        profile.addElement(leftStick);

        // 2. Right Trackpad (Free 360 Camera Mouse Look)
        ControlElement trackpad = new ControlElement(null);
        trackpad.setType(ControlElement.Type.TRACKPAD);
        trackpad.setShape(ControlElement.Shape.ROUND_RECT);
        trackpad.setX((short) 980);
        trackpad.setY((short) 420);
        trackpad.setScale(2.2f);
        profile.addElement(trackpad);

        // 3. Attack / Shoot (Left Mouse Button)
        ControlElement btnAttack = new ControlElement(null);
        btnAttack.setType(ControlElement.Type.BUTTON);
        btnAttack.setText("FIRE");
        btnAttack.setBindingAt(0, Binding.MOUSE_LEFT_BUTTON);
        btnAttack.setX((short) 1170);
        btnAttack.setY((short) 450);
        btnAttack.setScale(1.1f);
        profile.addElement(btnAttack);

        // 4. Aim (Right Mouse Button)
        ControlElement btnAim = new ControlElement(null);
        btnAim.setType(ControlElement.Type.BUTTON);
        btnAim.setText("AIM");
        btnAim.setBindingAt(0, Binding.MOUSE_RIGHT_BUTTON);
        btnAim.setX((short) 1050);
        btnAim.setY((short) 340);
        btnAim.setScale(1.0f);
        profile.addElement(btnAim);

        // 5. Sprint (Shift)
        ControlElement btnSprint = new ControlElement(null);
        btnSprint.setType(ControlElement.Type.BUTTON);
        btnSprint.setText("SPRINT");
        btnSprint.setBindingAt(0, Binding.KEY_SHIFT_LEFT);
        btnSprint.setX((short) 1160);
        btnSprint.setY((short) 580);
        btnSprint.setScale(1.1f);
        profile.addElement(btnSprint);

        // 6. Jump (Space)
        ControlElement btnJump = new ControlElement(null);
        btnJump.setType(ControlElement.Type.BUTTON);
        btnJump.setText("JUMP");
        btnJump.setBindingAt(0, Binding.KEY_SPACE);
        btnJump.setX((short) 1030);
        btnJump.setY((short) 620);
        btnJump.setScale(1.0f);
        profile.addElement(btnJump);

        // 7. Enter / Exit Vehicle (F / Enter)
        ControlElement btnEnter = new ControlElement(null);
        btnEnter.setType(ControlElement.Type.BUTTON);
        btnEnter.setText("ENTER");
        btnEnter.setBindingAt(0, Binding.KEY_F);
        btnEnter.setX((short) 1180);
        btnEnter.setY((short) 310);
        btnEnter.setScale(1.0f);
        profile.addElement(btnEnter);

        // 8. Crouch (C)
        ControlElement btnCrouch = new ControlElement(null);
        btnCrouch.setType(ControlElement.Type.BUTTON);
        btnCrouch.setText("CROUCH");
        btnCrouch.setBindingAt(0, Binding.KEY_C);
        btnCrouch.setX((short) 90);
        btnCrouch.setY((short) 380);
        btnCrouch.setScale(0.9f);
        profile.addElement(btnCrouch);

        // 9. Scoreboard (Tab)
        ControlElement btnTab = new ControlElement(null);
        btnTab.setType(ControlElement.Type.BUTTON);
        btnTab.setText("TAB");
        btnTab.setBindingAt(0, Binding.KEY_TAB);
        btnTab.setX((short) 60);
        btnTab.setY((short) 100);
        btnTab.setScale(0.85f);
        profile.addElement(btnTab);

        // 10. Chat (T key)
        ControlElement btnChat = new ControlElement(null);
        btnChat.setType(ControlElement.Type.BUTTON);
        btnChat.setText("CHAT");
        btnChat.setBindingAt(0, Binding.KEY_T);
        btnChat.setX((short) 160);
        btnChat.setY((short) 100);
        btnChat.setScale(0.85f);
        profile.addElement(btnChat);

        // 11. Esc (Menu)
        ControlElement btnEsc = new ControlElement(null);
        btnEsc.setType(ControlElement.Type.BUTTON);
        btnEsc.setText("ESC");
        btnEsc.setBindingAt(0, Binding.KEY_ESCAPE);
        btnEsc.setX((short) 1220);
        btnEsc.setY((short) 70);
        btnEsc.setScale(0.85f);
        profile.addElement(btnEsc);

        profile.save();
        return profile;
    }
}
