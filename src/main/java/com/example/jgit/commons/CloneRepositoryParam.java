package com.example.jgit.commons;

import lombok.Data;

@Data
public class CloneRepositoryParam {
    //本地路径
    private String localPath;

    //git路径
    private String remoteUrl;

    //登录git账号
    private String username;

    //登录git密码
    private String password;
}
