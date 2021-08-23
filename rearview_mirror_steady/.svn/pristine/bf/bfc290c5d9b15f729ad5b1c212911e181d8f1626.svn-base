package com.txznet.record.poi;

import java.util.Comparator;

import com.txznet.sdk.bean.BusinessPoiDetail;

public class PoiComparator_Score implements Comparator<Object> {
	@Override
	public int compare(Object lhs, Object rhs) {
		try {
			if(lhs instanceof BusinessPoiDetail && rhs instanceof BusinessPoiDetail){
				BusinessPoiDetail l = (BusinessPoiDetail) lhs;
				BusinessPoiDetail r = (BusinessPoiDetail) rhs;
				if (l.getScore() > r.getScore())
					return -1;
				if (l.getScore() < r.getScore())
					return 1;
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
