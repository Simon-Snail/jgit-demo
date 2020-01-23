package com.example.jgit.commons;

import lombok.Data;

@Data
public class PublishParam {
    //本地路径
    private String localPath;

    //merage from
    private String branchFrom;

    //merage to
    private String branchTo;

    //登录git账号
    private String username;

    //登录git密码
    private String password;
}
