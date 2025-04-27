// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.hjq.demo.chat.model.ait;

/** @ 显示的信息 因为可能是群里，也可能是数字人，所以抽离单独类 */
public class AitUserInfo {
  // 账号
  private String userJid;
  // 名称
  private String nickname;

  //@之后显示的信息
  private String aitName;

  public AitUserInfo(String account, String name, String aitName) {
    this.userJid = account;
    this.nickname = name;
    this.aitName = aitName;
  }

  public String getUserJid() {
    return userJid;
  }

  public String getNickname() {
    return nickname;
  }

  public String getAitName() {
    return aitName;
  }

  public void setUserJid(String userJid) {
    this.userJid = userJid;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }
}
