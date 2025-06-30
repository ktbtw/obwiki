package com.example.obwiki.rep;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

@Data
@ToString
@ApiModel("分页参数")
public class PageReq {
    @NotNull(message = "【页码】不能为空")
    @ApiModelProperty(value = "页码，从1开始", required = true, example = "1")
    private Integer page;

    @NotNull(message = "【每页条数】不能为空")
    @Max(value = 1000, message = "【每页条数】不能超过1000")
    @ApiModelProperty(value = "每页条数", required = true, example = "10")
    private Integer size;
}
