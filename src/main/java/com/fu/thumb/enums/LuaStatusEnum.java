package com.fu.thumb.enums;

import lombok.Getter;

@Getter
public enum LuaStatusEnum {

    SUCCESS(1L),
    FAIL(-1L),
    BLOG_NOT_EXIST(-2L),
    ;

    private final long value;

    LuaStatusEnum(long value) {
        this.value = value;
    }
}
