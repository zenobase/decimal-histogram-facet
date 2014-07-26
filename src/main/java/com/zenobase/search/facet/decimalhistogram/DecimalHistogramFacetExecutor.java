package com.zenobase.search.facet.decimalhistogram;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.elasticsearch.common.hppc.LongLongOpenHashMap;
import org.elasticsearch.common.recycler.Recycler;
import org.elasticsearch.index.fielddata.AtomicNumericFieldData;
import org.elasticsearch.index.fielddata.DoubleValues;
import org.elasticsearch.index.fielddata.IndexNumericFieldData;
import org.elasticsearch.search.facet.DoubleFacetAggregatorBase;
import org.elasticsearch.search.facet.FacetExecutor;
import org.elasticsearch.search.facet.InternalFacet;
import org.elasticsearch.search.facet.histogram.HistogramFacet.ComparatorType;
import org.elasticsearch.search.internal.SearchContext;

public class DecimalHistogramFacetExecutor extends FacetExecutor {

	private final IndexNumericFieldData<AtomicNumericFieldData> indexFieldData;
	private final ComparatorType comparatorType;
	private final double interval;
	private final double offset;

	final Recycler.V<LongLongOpenHashMap> counts;

	public DecimalHistogramFacetExecutor(IndexNumericFieldData<AtomicNumericFieldData> indexFieldData, double interval, double offset, ComparatorType comparatorType, SearchContext context) {
		this.indexFieldData = indexFieldData;
		this.interval = interval;
		this.offset = offset;
		this.comparatorType = comparatorType;
		this.counts = context.cacheRecycler().longLongMap(-1);
	}

	@Override
	public FacetExecutor.Collector collector() {
		return new Collector();
	}

	@Override
	public InternalFacet buildFacet(String facetName) {
		InternalDecimalHistogramFacet.DecimalEntry[] entries = new InternalDecimalHistogramFacet.DecimalEntry[counts.v().size()];
		final boolean[] states = counts.v().allocated;
		final long[] keys = counts.v().keys;
		final long[] values = counts.v().values;
		int entryIndex = 0;
		for (int i = 0; i < states.length; ++i) {
			if (states[i]) {
				entries[entryIndex++] = new InternalDecimalHistogramFacet.DecimalEntry(keys[i], values[i]);
			}
		}
		counts.close();
		return new InternalDecimalHistogramFacet(facetName, interval, offset, comparatorType, entries);
	}

	private class Collector extends FacetExecutor.Collector {

		private final HistogramProc histoProc;
		private DoubleValues values;

		public Collector() {
			this.histoProc = new HistogramProc(interval, offset, counts.v());
		}

		@Override
		public void setNextReader(AtomicReaderContext context) throws IOException {
			values = indexFieldData.load(context).getDoubleValues();
		}

		@Override
		public void collect(int doc) throws IOException {
			histoProc.onDoc(doc, values);
		}

		@Override
		public void postCollection() {

		}
	}

	private static class HistogramProc extends DoubleFacetAggregatorBase {

		private final double interval;
		private final double offset;
		private final LongLongOpenHashMap counts;

		public HistogramProc(double interval, double offset, LongLongOpenHashMap counts) {
			this.interval = interval;
			this.offset = offset;
			this.counts = counts;
		}

		@Override
		public void onValue(int docId, double value) {
			long bucket = (long) Math.floor(((value + offset) / interval));
			counts.addTo(bucket, 1);
		}
	}
}
