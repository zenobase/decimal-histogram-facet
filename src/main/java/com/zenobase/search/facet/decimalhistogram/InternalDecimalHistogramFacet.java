package com.zenobase.search.facet.decimalhistogram;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.bytes.HashedBytesArray;
import org.elasticsearch.common.hppc.LongLongOpenHashMap;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.recycler.Recycler;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.InternalFacet;
import org.elasticsearch.search.facet.histogram.HistogramFacet;
import org.elasticsearch.search.facet.histogram.HistogramFacet.ComparatorType;

public class InternalDecimalHistogramFacet extends InternalFacet implements DecimalHistogramFacet {

	private static final BytesReference STREAM_TYPE = new HashedBytesArray(Strings.toUTF8Bytes("decimalHistogram"));

	public static void registerStreams() {
		Streams.registerStream(STREAM, STREAM_TYPE);
	}

	static InternalFacet.Stream STREAM = new Stream() {

		@Override
		public Facet readFacet(StreamInput in) throws IOException {
			return readDecimalHistogramFacet(in);
		}
	};

	@Override
	public BytesReference streamType() {
		return STREAM_TYPE;
	}


	/**
	 * A decimal histogram entry representing a single entry within the result of a
	 * decimal histogram facet.
	 */
	public static class DecimalEntry implements DecimalHistogramFacet.Entry {

		long key;
		long count;

		public DecimalEntry(long key, long count) {
			this.key = key;
			this.count = count;
		}

		@Override
		public long getKey() {
			return key;
		}

		@Override
		public double getKey(double interval) {
			return key * interval;
		}

		@Override
		public long getCount() {
			return count;
		}

		@Override
		public double getTotal() {
			return Double.NaN;
		}

		@Override
		public long getTotalCount() {
			return 0;
		}

		@Override
		public double getMean() {
			return Double.NaN;
		}

		@Override
		public double getMin() {
			return Double.NaN;
		}

		@Override
		public double getMax() {
			return Double.NaN;
		}
	}

	double interval;
	double offset;
	HistogramFacet.ComparatorType comparatorType;
	DecimalEntry[] entries;

	InternalDecimalHistogramFacet() {

	}

	public InternalDecimalHistogramFacet(String name, double interval, double offset, HistogramFacet.ComparatorType comparatorType, DecimalEntry[] entries) {
		super(name);
		this.interval = interval;
		this.offset = offset;
		this.comparatorType = comparatorType;
		this.entries = entries;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public List<DecimalEntry> getEntries() {
		return Arrays.asList(entries);
	}

	@Override
	public Facet reduce(ReduceContext context) {
		List<Facet> facets = context.facets();
		if (facets.size() == 1) {
			InternalDecimalHistogramFacet facet = (InternalDecimalHistogramFacet) facets.get(0);
			Arrays.sort(facet.entries, facet.comparatorType.comparator());
			return facet;
		}

		Recycler.V<LongLongOpenHashMap> counts = context.cacheRecycler().longLongMap(-1);
		for (Facet facet : facets) {
			InternalDecimalHistogramFacet histoFacet = (InternalDecimalHistogramFacet) facet;
			for (DecimalEntry entry : histoFacet.entries) {
				counts.v().addTo(entry.getKey(), entry.getCount());
			}
		}
		final boolean[] states = counts.v().allocated;
		final long[] keys = counts.v().keys;
		final long[] values = counts.v().values;
		DecimalEntry[] entries = new DecimalEntry[counts.v().size()];
		int entryIndex = 0;
		for (int i = 0; i < states.length; ++i) {
			if (states[i]) {
				entries[entryIndex++] = new DecimalEntry(keys[i], values[i]);
			}
		}
		counts.close();

		Arrays.sort(entries, comparatorType.comparator());

        return new InternalDecimalHistogramFacet(getName(), interval, offset, comparatorType, entries);
	}

	private interface Fields {

		final XContentBuilderString _TYPE = new XContentBuilderString("_type");
		final XContentBuilderString ENTRIES = new XContentBuilderString("entries");
		final XContentBuilderString KEY = new XContentBuilderString("key");
		final XContentBuilderString COUNT = new XContentBuilderString("count");
	}

	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(getName());
		builder.field(Fields._TYPE, DecimalHistogramFacet.TYPE);
		builder.startArray(Fields.ENTRIES);
		for (HistogramFacet.Entry entry : entries) {
			toXContent(entry, builder);
		}
		builder.endArray();
		builder.endObject();
		return builder;
	}

	private void toXContent(HistogramFacet.Entry entry, XContentBuilder builder) throws IOException {
		builder.startObject();
		builder.field(Fields.KEY, entry.getKey() * interval);
		builder.field(Fields.COUNT, entry.getCount());
		builder.endObject();
	}

	public static InternalDecimalHistogramFacet readDecimalHistogramFacet(StreamInput in) throws IOException {
		InternalDecimalHistogramFacet facet = new InternalDecimalHistogramFacet();
		facet.readFrom(in);
		return facet;
	}

	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		comparatorType = ComparatorType.fromId(in.readByte());
		int size = in.readVInt();
		entries = new DecimalEntry[size];
		for (int i = 0; i < size; i++) {
			entries[i] = new DecimalEntry(in.readLong(), in.readVLong());
		}
	}

	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeByte(comparatorType.id());
		out.writeVInt(entries.length);
		for (DecimalEntry entry : entries) {
			out.writeLong(entry.key);
			out.writeVLong(entry.count);
		}
	}
}
