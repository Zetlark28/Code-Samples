package it.zetlark.mongodb.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartEndFilterDto<T> {

    private T start;
    private T end;

}
