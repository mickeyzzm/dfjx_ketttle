package org.seaboxdata.systemmng.auth.vo;

import java.io.Serializable;

/**
 * @author makaiyu
 * @date 2019/5/13 18:02
 */
public class OauthUserDTO implements Serializable {

    private static final long serialVersionUID = 74335887421213094L;
    /** 主键id */
    private Long id;

    /** 登录账号 */
    private String username;

    /** 名称 */
    private String name;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
