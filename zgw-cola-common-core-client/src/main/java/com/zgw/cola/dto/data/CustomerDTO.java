package com.zgw.cola.dto.data;

import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CustomerDTO{
    private String customerId;
    private String memberId;
    private String customerName;
    private String customerType;
    @NotEmpty
    private String companyName;
    @NotEmpty
    private String source;
}
