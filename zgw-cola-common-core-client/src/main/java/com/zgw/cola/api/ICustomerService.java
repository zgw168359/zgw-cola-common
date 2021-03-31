package com.zgw.cola.api;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.zgw.cola.dto.CustomerAddCmd;
import com.zgw.cola.dto.CustomerListByNameQry;
import com.zgw.cola.dto.data.CustomerDTO;

public interface ICustomerService {

    public Response addCustomer(CustomerAddCmd customerAddCmd);

    public MultiResponse<CustomerDTO> listByName(CustomerListByNameQry customerListByNameQry);
}
