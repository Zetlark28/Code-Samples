package it.zetlark.mongodb.common.mapper;

import it.zetlark.mongodb.common.dto.BaseDto;
import it.zetlark.mongodb.common.entity.BaseDocument;

import java.util.List;

public interface BaseMapper <DTO extends BaseDto, DOC extends BaseDocument>{

	DTO toDTO(DOC doc);
	List<DTO> toDTOList (List<DOC> docList);
	DOC toEntity(DTO dto);
}
