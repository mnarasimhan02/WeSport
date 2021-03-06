package com.my.game.wesport.FireChatHelper;

import com.my.game.wesport.R;

import java.util.Random;


public class ChatHelper {

    private static final Random randomAvatarGenerator = new Random();
    private static final int NUMBER_OF_AVATAR = 3;

    /*Generate an avatar randomly*/
    public static int  generateRandomAvatarForUser(){
        return randomAvatarGenerator.nextInt(NUMBER_OF_AVATAR);
    }

    /*Get avatar id*/

    public static int getDrawableAvatarId(int givenRandomAvatarId){

        switch (givenRandomAvatarId){

            case 0:
                return R.mipmap.ic_avatar_blue;
            case 1:
                return R.mipmap.ic_avatar_green;
            default:
                return R.mipmap.ic_avatar_purple;
        }
    }

}
