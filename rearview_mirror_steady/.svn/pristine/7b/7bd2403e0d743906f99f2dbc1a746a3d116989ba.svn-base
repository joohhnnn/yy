package com.txznet.txz.ui.widget.mov;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.view.ScaleImageView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.R;

public class FilmItemView extends LinearLayout {
	public static final int KEY_LAYOUT_ID = 0x001;

	View mContentView;
	ViewHolder mHolder;


	public FilmItemView(Context context) {
		this(context, null);
	}

	public FilmItemView(Context context, AttributeSet attr) {
		this(context, attr, 0);
	}

	public FilmItemView(Context context, AttributeSet attr, int defValue) {
		super(context, attr, defValue);

		init();
	}

	private void init() {
		removeAllViews();
		mContentView = inflate(getContext(), R.layout.view_film_item, this);
		if (mHolder == null) {
			mHolder = new ViewHolder();
		}

		mHolder.mCineBillLayout = (RelativeLayout) mContentView.findViewById(R.id.cinema_bill_rl);
		mHolder.mCineBillIv = (ScaleImageView) mContentView.findViewById(R.id.cinema_bill_iv);
		mHolder.mCineTitleTv = (TextView) mContentView.findViewById(R.id.cinema_title_tv);
		mHolder.mCineScoreLayout = (LinearLayout) mContentView.findViewById(R.id.cinema_score_ly);
		mHolder.mCineScoreIv = (ImageView) mContentView.findViewById(R.id.cinema_score_iv);
		mHolder.mCineScoreTvPref = (TextView) mContentView.findViewById(R.id.cinema_score_tv_pref);
		mHolder.mCineScoreTvAft = (TextView) mContentView.findViewById(R.id.cinema_score_tv_aft);

		mHolder.mCineBillIv.setMode(ScaleImageView.MODE_AUTO_HEIGHT);
		mHolder.mCineBillIv.setScale(1.5f);
		mHolder.tvNoScore = (TextView) mContentView.findViewById(R.id.cinema_no_score_tv);
		mHolder.tvNoScore.setText("暂无评分");
		TextViewUtil.setTextSize(mHolder.tvNoScore, LayouUtil.getDimen("y24"));
		TextViewUtil.setTextColor(mHolder.tvNoScore, Color.parseColor("#FFFFFF"));

		this.setTag(R.string.key_cinema_holder, mHolder);


	}

	public static class ViewHolder {
		public RelativeLayout mCineBillLayout;
		public ScaleImageView mCineBillIv;
		public TextView mCineTitleTv;
		public LinearLayout mCineScoreLayout;
		public ImageView mCineScoreIv;
		public TextView mCineScoreTvPref;
		public TextView mCineScoreTvAft;
		public TextView tvNoScore;

		public void clear() {
			mCineScoreIv.setVisibility(View.INVISIBLE);
			mCineBillIv.setVisibility(View.INVISIBLE);
			mCineTitleTv.setText("");
			mCineScoreTvPref.setText("");
			mCineScoreTvAft.setText("");
		}

		public void setTitle(String name) {
			mCineTitleTv.setVisibility(View.VISIBLE);
			mCineTitleTv.setText(name);
		}

		public void setScore(double score) {
			if(score > 0){
				String sc = String.format("%.1f", score);
				if (sc.contains(".")) {
					String[] scoreArray = sc.split("\\.");
					if (scoreArray != null && scoreArray.length > 1) {
						mCineScoreTvPref.setText(scoreArray[0]);
						mCineScoreTvAft.setText("." + scoreArray[1]);
					}
				} else {
					mCineScoreTvPref.setText(sc);
					mCineScoreTvAft.setText("");
				}

				mCineScoreIv.setImageResource(getSoreMark(score));
				if (mCineScoreIv.getVisibility() != View.VISIBLE) {
					mCineScoreIv.setVisibility(View.VISIBLE);
				}
				mCineScoreLayout.setVisibility(View.VISIBLE);
				tvNoScore.setVisibility(View.GONE);
			}else {
				mCineScoreLayout.setVisibility(View.GONE);
				tvNoScore.setVisibility(View.VISIBLE);
			}

		}

		private int getSoreMark(double score) {
			if (score < 1.0f) {
				return R.drawable.dz_icon_star0;
			} else if (score < 2.0f) {
				return R.drawable.dz_icon_star1;
			} else if (score < 3.0f) {
				return R.drawable.dz_icon_star2;
			} else if (score < 4.0f) {
				return R.drawable.dz_icon_star3;
			} else if (score < 5.0f) {
				return R.drawable.dz_icon_star4;
			} else if (score < 6.0f) {
				return R.drawable.dz_icon_star5;
			} else if (score < 7.0f) {
				return R.drawable.dz_icon_star6;
			} else if (score < 8.0f) {
				return R.drawable.dz_icon_star7;
			} else if (score < 9.0f) {
				return R.drawable.dz_icon_star8;
			} else if (score < 10.0f) {
				return R.drawable.dz_icon_star9;
			} else {
				return R.drawable.dz_icon_star10;
			}
		}
	}
}