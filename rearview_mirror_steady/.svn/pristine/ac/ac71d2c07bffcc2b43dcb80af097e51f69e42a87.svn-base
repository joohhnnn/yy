package com.txznet.music.adpter;

import java.util.List;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.bean.response.Album;
import com.txznet.music.fragment.HomepageFragment;
import com.txznet.music.fragment.base.BaseFragment;
import com.txznet.music.ui.MediaPlayerActivity;
import com.txznet.music.utils.AnimationUtils;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.ImageUtils;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.NavListener;
import com.txznet.music.widget.ShadeImageView;

public class ItemAlbumAdapter extends RecyclerView.Adapter<ItemAlbumAdapter.ViewHolder> implements NavListener {

	private BaseFragment baseFragment;
	private List<Album> albums;
	private OnItemClickListener listener;

	private Album currentAlbum;// 当前播放的专辑，用于变色

	// WinConfirm winConfirm;

	public ItemAlbumAdapter(BaseFragment baseFragment, List<Album> albums) {
		super();
		this.baseFragment = baseFragment;
		this.albums = albums;
		// winConfirm = new WinConfirm(true) {
		//
		// @Override
		// public void onClickOk() {
		// winConfirm.dismiss();
		// }
		// }.setMessage("厂家未授权不能使用，请联系厂家");
	}

	public class ViewHolder extends RecyclerView.ViewHolder {

		private TextView tvIntro;
		private ShadeImageView ivType;
		public LinearLayout llAlbum;

		public ViewHolder(View itemView) {
			super(itemView);
			ivType = (ShadeImageView) itemView.findViewById(R.id.type_iv);
			tvIntro = (TextView) itemView.findViewById(R.id.intro_tv);
			llAlbum = (LinearLayout) itemView.findViewById(R.id.album_ll);
		}

		public void setIntro(String text) {
			if (StringUtils.isEmpty(text)) {
				tvIntro.setVisibility(View.GONE);
				return;
			}
			tvIntro.setVisibility(View.VISIBLE);
			// tvIntro.setHeight(getFontHeight(tvIntro.getTextSize()) * 3);
			tvIntro.setText(text);
		}

		public void setImageResource(int resource) {
			if (0 == resource) {
				return;
			}
			ivType.setImageResource(resource);
		}

		public ImageView getImageView() {
			return ivType;
		}

		public TextView getTitle() {
			return tvIntro;
		}

		public void setOnClickListener(OnClickListener listener) {
			if (null != listener) {
				ivType.setOnClickListener(listener);
			}
		}

	}

	private boolean isShowLoading;

	/**
	 * 是否加载中
	 * 
	 * @param show
	 */
	public void setShowLoading(boolean show) {
		isShowLoading = show;
	}

	public boolean isShowLoading() {
		return isShowLoading;
	}

	@Override
	public int getItemCount() {
		if (CollectionUtils.isNotEmpty(albums)) {
			if (isShowLoading) {
				return albums.size() + 1;
			}
			return albums.size();
		}
		return 0;
	}

	private int mImageViewWidth = 0;
	
	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {
		Album album;
		if (isShowLoading && position >= albums.size()) {
			int width = baseFragment.getResources().getDimensionPixelOffset(R.dimen.x88);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
			holder.getImageView().setLayoutParams(params);
			holder.getImageView().setImageResource(R.drawable.fm_loading);
			holder.getImageView().startAnimation(AnimationUtils.getRotateAnimation());
			holder.getTitle().setBackground(new ColorDrawable(android.R.color.transparent));
			holder.getTitle().setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
			LinearLayout.LayoutParams params1 = (android.widget.LinearLayout.LayoutParams) holder.getTitle()
					.getLayoutParams();
			params1.topMargin = baseFragment.getResources().getDimensionPixelOffset(R.dimen.y10);
			holder.getTitle().setLayoutParams(params1);
			holder.setIntro("正在加载");
			holder.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// ToastUtils.showShort("wow");
				}
			});
		} else {
			album = albums.get(position);
			ImageLoader.getInstance().displayImage(album.getLogo(), holder.getImageView(),
					ImageUtils.initDefault(R.drawable.fm_item_default, 0));
			if(mImageViewWidth == 0){
				holder.llAlbum.addOnLayoutChangeListener(new OnLayoutChangeListener() {

					@Override
					public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
							int oldRight, int oldBottom) {
						holder.llAlbum.removeOnLayoutChangeListener(this);
						int height = bottom - top;
						int width = (int) (height * 0.7);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
						holder.getImageView().setLayoutParams(params);
						LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT);
						holder.getTitle().setLayoutParams(params1);
						holder.getTitle().setWidth(width);

						holder.getTitle().setTextSize(TypedValue.COMPLEX_UNIT_PX, 26);
						mImageViewWidth = width;
					}
				});
			}else{
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mImageViewWidth, mImageViewWidth);
				holder.getImageView().setLayoutParams(params);
				LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);
				holder.getTitle().setLayoutParams(params1);
				holder.getTitle().setWidth(mImageViewWidth);

				holder.getTitle().setTextSize(TypedValue.COMPLEX_UNIT_PX, 26);
			}
			

			holder.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (albums == null || albums.size() <= position) {
						LogUtil.loge("ItemAlbumAdapter index out of bounds index:" + position + " size:"
								+ (albums == null ? -1 : albums.size()));
						return;
					}
					currentAlbum = albums.get(position);
					SharedPreferencesUtils.setAudioSource(Constant.TYPE_SHOW);
					if (null != listener) {
						listener.onItemClick(null, v, position, 0);
						notifyDataSetChanged();
					} else {
						if (Utils.isNetworkConnected(GlobalContext.get())) {
							Intent intent = new Intent(baseFragment.getActivity(), MediaPlayerActivity.class);
							intent.putExtra(Constant.PAGENAMEEXTRA, currentAlbum);
							baseFragment.getActivity().startActivity(intent);// 跳转到播放器页面
						} else {
							TtsUtil.speakResource("RS_VOICE_SPEAK_NETNOTCON_TIPS",
									Constant.RS_VOICE_SPEAK_NETNOTCON_TIPS);
						}
					}

				}
			});

			if (album == currentAlbum) {
				holder.getTitle()
						.setBackgroundColor(baseFragment.getResources().getColor(R.color.album_item_selected_bg));
			} else {
				holder.getTitle().setBackgroundColor(baseFragment.getResources().getColor(R.color.album_item_bg));
			}

			holder.setIntro(album.getName());

			if(album.getCategoryID() != mLastCategoryId){
				mLastCategoryId = album.getCategoryID();
				mNavFocusIndex = 0;
			}
			
			if (isFocus && mNavFocusIndex == position) {
				holder.itemView.setBackgroundResource(R.drawable.shape_focus_rect);
			} else {
				holder.itemView.setBackground(null);
			}
		}
	}
	
	private int mLastCategoryId = 0;

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}

	public Object getItem(int position) {
		if (position < albums.size()) {
			return albums.get(position);
		}
		return null;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music_grid_view_1, parent, false);
		return new ViewHolder(v);
	}

	public int getFontHeight(float fontSize) {
		Paint paint = new Paint();
		paint.setTextSize(fontSize);
		FontMetrics fm = paint.getFontMetrics();
		return (int) Math.ceil(fm.descent - fm.top) + 2;
	}

	private int mNavFocusIndex = -1;
	private boolean isFocus = false;
	private OnRefreshListener mRefreshListener = null;

	@Override
	public int onNext() {
		if (mNavFocusIndex < albums.size() - 1) {
			mNavFocusIndex++;
			notifyDataSetChanged();
		}
		if (mNavFocusIndex == albums.size() - 1 && mRefreshListener != null) {
			mRefreshListener.onRefresh(mNavFocusIndex);
		}
		return mNavFocusIndex;
	}

	@Override
	public int onPrev() {
		if (mNavFocusIndex > 0) {
			mNavFocusIndex--;
			notifyDataSetChanged();
		}
		return mNavFocusIndex;
	}

	@Override
	public void onClick() {
		if (mNavFocusIndex >= 0 && mNavFocusIndex < albums.size()) {
			listener.onItemClick(null, null, mNavFocusIndex, 0);
		}
	}

	@Override
	public void setFocus(boolean isFocus) {
		this.isFocus = isFocus;
		if (isFocus && mNavFocusIndex == -1) {
			mNavFocusIndex = 0;
		}
		notifyDataSetChanged();
	}

	public void setOnRefreshListener(OnRefreshListener listener) {
		this.mRefreshListener = listener;
	}
}
