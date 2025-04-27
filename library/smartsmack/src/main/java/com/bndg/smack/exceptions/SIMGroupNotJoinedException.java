package com.bndg.smack.exceptions;

/**
 * @author r
 * @date 2024/8/26
 * @description 群组未加入异常
 */
public class SIMGroupNotJoinedException extends Exception {
    public boolean isCanNotJoin() {
        return canNotJoin;
    }

    public void setCanNotJoin(boolean canNotJoin) {
        this.canNotJoin = canNotJoin;
    }

    private boolean canNotJoin = false;
}
