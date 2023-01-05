package it.zetlark.mongodb.domain;

import it.zetlark.mongodb.common.mapper.BaseMapper;
import it.zetlark.mongodb.common.repository.MongoSearchOperationsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class ExampleMongoSearchOperationsRepository extends MongoSearchOperationsRepository<ExampleResponseDto, ExampleFilterDto, ExampleDoc> {


	public ExampleMongoSearchOperationsRepository(MongoOperations mongoOperations,
			ExampleMapper mapper) {
		super(mongoOperations, mapper);
	}

	@Override
	public Page<ExampleResponseDto> search(ExampleFilterDto filter, Pageable pageable) {
		final Query query = super.prepareSearchQuery(filter, pageable,false);
		final List<ExampleDoc> list = mongoOperations.find(query, ExampleDoc.class);
		final Query queryCount = super.prepareSearchQuery(filter, null, false);
		final long count = mongoOperations.count(queryCount, ExampleDoc.class);
		return new PageImpl<>(mapper.toDTOList(list), pageable, count);
	}
}
