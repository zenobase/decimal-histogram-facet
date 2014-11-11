package com.zenobase.search.facet.decimalhistogram;

import java.io.IOException;

import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.fielddata.AtomicNumericFieldData;
import org.elasticsearch.index.fielddata.IndexNumericFieldData;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.search.facet.FacetExecutor;
import org.elasticsearch.search.facet.FacetExecutor.Mode;
import org.elasticsearch.search.facet.FacetParser;
import org.elasticsearch.search.facet.FacetPhaseExecutionException;
import org.elasticsearch.search.facet.histogram.HistogramFacet.ComparatorType;
import org.elasticsearch.search.internal.SearchContext;

public class DecimalHistogramFacetParser extends AbstractComponent implements FacetParser {

	@Inject
	public DecimalHistogramFacetParser(Settings settings) {
		super(settings);
		InternalDecimalHistogramFacet.registerStreams();
	}

	@Override
	public String[] types() {
		return new String[] {
			DecimalHistogramFacet.TYPE
		};
	}

	@Override
	public Mode defaultMainMode() {
		return FacetExecutor.Mode.COLLECTOR;
	}

	@Override
	public Mode defaultGlobalMode() {
		return FacetExecutor.Mode.COLLECTOR;
	}

	@Override
	public FacetExecutor parse(String facetName, XContentParser parser, SearchContext context) throws IOException {

		String field = null;
		double interval = 0.0;
		double offset = 0.0;
		ComparatorType comparatorType = ComparatorType.KEY;

		String currentName = parser.currentName();
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentName = parser.currentName();
			} else if (token.isValue()) {
				if ("field".equals(currentName)) {
					field = parser.text();
				} else if ("interval".equals(currentName)) {
					interval = parser.doubleValue();
				} else if ("offset".equals(currentName)) {
					offset = parser.doubleValue();
				} else if ("order".equals(currentName) || "comparator".equals(currentName)) {
					comparatorType = ComparatorType.fromString(parser.text());
				}
			}
		}

		if (field == null) {
			throw new FacetPhaseExecutionException(facetName, "[field] is required for decimal histogram facet");
		}
		if (interval <= 0.0) {
			throw new FacetPhaseExecutionException(facetName, "[interval] must be greater than 0.0");
		}
		FieldMapper<AtomicNumericFieldData> fieldMapper = context.smartNameFieldMapper(field);
        if (fieldMapper == null) {
            throw new FacetPhaseExecutionException(facetName, "failed to find mapping for [" + field + "]");
        }
        IndexNumericFieldData indexFieldData = context.fieldData().getForField(fieldMapper);
		return new DecimalHistogramFacetExecutor(indexFieldData, interval, offset, comparatorType, context);
	}
}
