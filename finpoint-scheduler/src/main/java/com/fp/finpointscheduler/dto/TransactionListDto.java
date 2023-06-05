package com.fp.finpointscheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionListDto {

    public String bank_tran_id;
    public String fintech_use_num;
    public String inquiry_type;
    public String inquiry_base;
    public String from_date;
    public String from_time;
    public String to_date;
    public String to_time;
    public String sort_order;
    public String tran_dtime;
}
