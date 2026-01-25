package com.example.demo.dtos;

import java.util.Objects;
import java.util.UUID;

public class UserDeviceDTO {
    private Long id;
    private UUID userid;
    private UUID deviceid;

    public UserDeviceDTO() {}
    public UserDeviceDTO(Long id,UUID userid, UUID deviceid) {
        this.id = id;
        this.userid = userid;
        this.deviceid = deviceid;

    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getUserid() { return userid; }
    public void setUserid(UUID userid) { this.userid = userid; }

    public UUID getDeviceid() { return deviceid; }
    public void setDeviceid(UUID deviceid) { this.deviceid = deviceid; }


}
