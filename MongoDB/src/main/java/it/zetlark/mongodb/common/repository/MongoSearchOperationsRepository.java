package it.zetlark.mongodb.common.repository;

import it.zetlark.mongodb.common.dto.BaseDto;
import it.zetlark.mongodb.common.dto.StartEndFilterDto;
import it.zetlark.mongodb.common.entity.BaseDocument;
import it.zetlark.mongodb.common.enums.LanguageEnum;
import it.zetlark.mongodb.common.mapper.BaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public abstract class MongoSearchOperationsRepository<ResponseDTO extends BaseDto, FilterDto extends BaseDto, DOC extends BaseDocument> {

	protected final MongoOperations mongoOperations;

	protected final BaseMapper<ResponseDTO, DOC> mapper;


	@Transactional(readOnly = true)
	public abstract Page<ResponseDTO> search(final FilterDto filter, final Pageable pageable);

	/**
	 * construct search query by filter fields.
	 * Multilingual = true if doc have name field like  'name':{'it':valore, 'en':value}
	 *
	 * @param filter
	 * @param pageable
	 * @return
	 */
	protected Query prepareSearchQuery(final FilterDto filter, final Pageable pageable, boolean multilingual) {
		final Query query;
		if (pageable != null) {
			query = new Query().with(pageable);
		} else {
			query = new Query();
		}

		Arrays.stream(ReflectionUtils.getDeclaredMethods(filter.getClass()))
				.filter(method -> method.getName().startsWith("get"))
				.forEach(method -> {
					final Object value = ReflectionUtils.invokeMethod(method, filter);
					final String fieldName = StringUtils.uncapitalize(method.getName().replace("get", ""));
					final Optional<Criteria> criteria = this.getCriteria(fieldName, value, multilingual);
					criteria.ifPresent(query::addCriteria);
				});

		return query;
	}


	/**
	 * if multilingual == true then doc should have name map for multilingual
	 * @param fieldName
	 * @param value
	 * @param multilingual
	 * @return
	 */
	private Optional<Criteria> getCriteria(final String fieldName, final Object value, boolean multilingual) {

		if (Objects.isNull(value)) {
			return Optional.empty();
		}

		final Criteria criteria = Criteria.where(fieldName);

		if (multilingual && (fieldName.equals("nameFilter") || fieldName.equals("name"))) {
			return getMultilingualNameFieldCriteria(value);
		}

		if (value instanceof StartEndFilterDto) {
			final StartEndFilterDto startEnd = (StartEndFilterDto) value;
			if (Objects.nonNull(startEnd.getStart())) {
				criteria.gte(startEnd.getStart());
			}
			if (Objects.nonNull(startEnd.getEnd())) {
				criteria.lte(startEnd.getEnd());
			}
		} else if (value instanceof Collection && !((Collection) value).isEmpty()) {
			criteria.in((Collection<?>) value);
		} else if (value instanceof String) {
			criteria.regex(value.toString(), "i");
		} else {
			criteria.is(value);
		}

		return Optional.of(criteria);
	}

	/**
	 * usefull when field name is a map for multilang like name : {'it': 'valore', 'en':'value}
	 * @param value
	 * @return
	 */
	private Optional<Criteria> getMultilingualNameFieldCriteria(final Object value) {
		List<LanguageEnum> languages = List.of(LanguageEnum.values());
		Criteria finalCriteria = new Criteria();
		List<Criteria> nameCriteriaList = new ArrayList<>();
		for (int i = 0; i < languages.size(); i++) {
			final Criteria criteria = Criteria.where("name." + languages.get(i));
			criteria.regex(value.toString(), "i");
			nameCriteriaList.add(criteria);

		}
		finalCriteria.orOperator(nameCriteriaList);
		return Optional.of(finalCriteria);
	}

	private List<Criteria> getLanguageCriteriaList(String fieldName, final Object value) {
		List<LanguageEnum> languages = List.of(LanguageEnum.values());
		List<Criteria> languageCriteriaList = new ArrayList<>();
		for (int i = 0; i < languages.size(); i++) {
			final Criteria criteria = Criteria.where(fieldName + "." + languages.get(i));
			criteria.regex(value.toString(), "i");
			languageCriteriaList.add(criteria);
		}
		return languageCriteriaList;
	}


}
