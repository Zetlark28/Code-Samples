package it.zetlark.mongodb.domain;

import it.zetlark.mongodb.common.mapper.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExampleMapper extends BaseMapper<ExampleResponseDto, ExampleDoc> {
}
