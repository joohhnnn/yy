package com.txznet.record.poi;

import java.util.Comparator;

import com.txznet.sdk.bean.BusinessPoiDetail;

public class PoiComparator_Price implements Comparator<Object> {
	@Override
	public int compare(Object lhs, Object rhs) {
		try {
			if (lhs instanceof BusinessPoiDetail && rhs instanceof BusinessPoiDetail) {
				BusinessPoiDetail l = (BusinessPoiDetail) lhs;
				BusinessPoiDetail r = (BusinessPoiDetail) rhs;
				if (l.getAvgPrice() == 0 && r.getAvgPrice() != 0)
					return 1;
				if (l.getAvgPrice() != 0 && r.getAvgPrice() == 0)
					return -1;
				if (l.getAvgPrice() > r.getAvgPrice())
					return 1;
				if (l.getAvgPrice() < r.getAvgPrice())
					return -1;
				return 0;
			}

			if (lhs instanceof BusinessPoiDetail) {
				return 1;
			}

			if (rhs instanceof BusinessPoiDetail) {
				return -1;
			}

		} catch (Exception e) {
		}
		return 0;
	}

}
