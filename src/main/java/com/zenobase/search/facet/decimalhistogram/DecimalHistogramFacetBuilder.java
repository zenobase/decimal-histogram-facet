package com.zenobase.search.facet.decimalhistogram;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.facet.histogram.HistogramFacet.ComparatorType;

public class DecimalHistogramFacetBuilder extends FacetBuilder {

	private final String field;
	private final double interval;
	private final double offset;
	private final ComparatorType comparatorType;

	public DecimalHistogramFacetBuilder(String name, String field, double interval, double offset, ComparatorType comparatorType) {
		super(name);
		this.field = field;
		this.interval = interval;
		this.offset = offset;
		this.comparatorType = comparatorType;
	}

	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(name);
		builder.startObject(DecimalHistogramFacet.TYPE);
		builder.field("field", field);
		builder.field("interval", interval);
		builder.field("offset", offset);
		if (comparatorType != null) {
			builder.field("order", comparatorType.description());
		}
		builder.endObject();
		addFilterFacetAndGlobal(builder, params);
		builder.endObject();
		return builder;
	}
}
