package com.txznet.record.poi;

import java.util.Comparator;

import com.txznet.sdk.bean.Poi;

public class PoiComparator_Distance implements Comparator<Object> {
	@Override
	public int compare(Object lhs, Object rhs) {
		try {
			Poi l = (Poi) lhs;
			Poi r = (Poi) rhs;
			if (l.getDistance() > r.getDistance())
				return 1;
			if (l.getDistance() < r.getDistance())
				return -1;
		} catch (Exception e) {
		}
		return 0;
	}

}
