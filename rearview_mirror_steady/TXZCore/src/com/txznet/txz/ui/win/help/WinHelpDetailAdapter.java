package com.txznet.txz.ui.win.help;

import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.record.adapter.ChatDisplayAdapter;
import com.txznet.txz.R;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.util.LanguageConvertor;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * get data from res,and add view to list.
 *
 */
public class WinHelpDetailAdapter extends ChatDisplayAdapter  {
	
	LayoutInflater mLayoutInflater;
	private Context mContext;
	
	List<HelpDetail> helpDetails;
	private boolean isFromFile = false;
	private boolean canOpenDetail = false;
	
	
	public WinHelpDetailAdapter(Context context,List<HelpDetail> helpDetails, boolean isFromFile ,boolean canOpenDetail) {
		super(context, helpDetails);
		mLayoutInflater = LayoutInflater.from(context);
		this.mContext = context;
		this.isFromFile = isFromFile;
		this.canOpenDetail = canOpenDetail;
		this.helpDetails = new ArrayList<HelpDetail>();
		if (helpDetails!=null) {
			this.helpDetails.addAll(helpDetails);
		}
		setDisplayList(this.helpDetails);
	}
	
	public void setHelpDetails(List<HelpDetail> helpDetails) {
		this.helpDetails.clear();
		this.helpDetails.addAll(helpDetails);
	}

//    @Override
//    public int getGroupCount() {
//        return helpDetails.size();
//    }
//
//    @Override
//    public int getChildrenCount(int groupPosition) {
//        return helpDetails.get(groupPosition).desps.length;
//    }
//
//    @Override
//    public HelpDetail getGroup(int groupPosition) {
//        return helpDetails.get(groupPosition);
//    }
//
//    @Override
//    public String getChild(int groupPosition, int childPosition) {
//        return helpDetails.get(groupPosition).desps[childPosition];
//    }
//
//    @Override
//    public long getGroupId(int groupPosition) {
//        return groupPosition;
//    }
//
//    @Override
//    public long getChildId(int groupPosition, int childPosition) {
//        return childPosition;
//    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

//    @Override
//    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
//        GroupHolder groupHolder = null;
//        if (null == convertView) {
//            convertView = mLayoutInflater.inflate(R.layout.win_help_detail_group, null);
//            groupHolder = new GroupHolder();
//            groupHolder.mRoot = (FrameLayout) convertView.findViewById(R.id.help_group_layout);
//            groupHolder.mArrow = (ImageView) convertView.findViewById(R.id.help_group_arrow);
//            groupHolder.mTitle = (TextView) convertView.findViewById(R.id.help_group_title);
//            groupHolder.mIntro = (TextView) convertView.findViewById(R.id.help_group_intro);
//            groupHolder.mIcon = (ImageView) convertView.findViewById(R.id.help_group_icon);
//            groupHolder.mViewDivider = (View) convertView.findViewById(R.id.help_group_divider);
//            convertView.setTag(groupHolder);
//            prepareSetLayoutParams(convertView);
//        } else {
//            groupHolder = (GroupHolder) convertView.getTag();
//        }
//        if (!isExpanded) {
////            groupHolder.mRoot.setBackgroundResource(R.drawable.shape_help_group_back);
//            groupHolder.mArrow.setRotation(0);
////            groupHolder.mViewDivider.setVisibility(View.GONE);
////            groupHolder.mIntro.setVisibility(View.VISIBLE);
//            
//            if (groupPosition == (getGroupCount() -1)) {
//            	groupHolder.mViewDivider.setVisibility(View.GONE);
//    		}else {
//    			groupHolder.mViewDivider.setVisibility(View.VISIBLE);
//    		}
//        } else {
//        	
////            groupHolder.mRoot.setBackgroundResource(R.drawable.shape_help_group_back_expanded);
//            groupHolder.mArrow.setRotation(180);
//            groupHolder.mViewDivider.setVisibility(View.VISIBLE);
////            groupHolder.mIntro.setVisibility(View.GONE);
//        }
//        
//        
//			
//        groupHolder.mIcon.setImageResource(getGroup(groupPosition).iconResId);
//        groupHolder.mTitle.setText(""+getGroup(groupPosition).title);
//        groupHolder.mIntro.setText(getGroup(groupPosition).intro);
//        return convertView;
//    }

//    @Override
//    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
//        ItemHolder holder = null;
//        if (null == convertView) {
//            convertView = mLayoutInflater.inflate(R.layout.win_help_detail_desp, null);
//            holder = new ItemHolder();
//            holder.mRoot = (FrameLayout) convertView.findViewById(R.id.help_detail_desp_layout);
//            holder.mText = (TextView) convertView.findViewById(R.id.help_detail_desp);
//            holder.mViewDivider = convertView.findViewById(R.id.help_child_divider);
//            convertView.setTag(holder);
//            prepareSetLayoutParams(convertView);
//        } else {
//            holder = (ItemHolder) convertView.getTag();
//        }
//        
//        if ((groupPosition == (getGroupCount()-1))&&(childPosition == (getChildrenCount(groupPosition)-1))) {
//        	holder.mViewDivider.setVisibility(View.GONE);
//		}else {
//			holder.mViewDivider.setVisibility(View.VISIBLE);
//		}
//
////        if(childPosition == 0){
////            holder.mRoot.setPadding(0, (int) mContext.getResources().getDimension(R.dimen.y10), 0, 0);
////        }
////
////        if(isLastChild){
//////            holder.mRoot.setBackgroundResource(R.drawable.shape_help_item_back_last);
////            holder.mRoot.setPadding(0, 0, 0, (int) mContext.getResources().getDimension(R.dimen.y10));
////        }else{
//////            holder.mRoot.setBackgroundColor(0xff282828);
////            holder.mRoot.setPadding(0, (int) mContext.getResources().getDimension(R.dimen.y10), 0, 0);
////        }
//
//        String str = getChild(groupPosition, childPosition);
//
////        if(str.startsWith("“")){
////            holder.mText.setGravity(Gravity.LEFT);
////        }else{
////            holder.mText.setGravity(Gravity.LEFT);
////        }
//        holder.mText.setText("\""+str+"\"");
//
//        return convertView;
//    }
//
//    @Override
//    public boolean isChildSelectable(int groupPosition, int childPosition) {
//        return false;
//    }
//
//    @Override
//    public boolean areAllItemsEnabled() {
//        return false;
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return false;
//    }
//
//    @Override
//    public void onGroupExpanded(int groupPosition) {
//
//    }
//
//    @Override
//    public void onGroupCollapsed(int groupPosition) {
//
//    }
//
//    @Override
//    public long getCombinedChildId(long groupId, long childId) {
//        return 0;
//    }
//
//    @Override
//    public long getCombinedGroupId(long groupId) {
//        return 0;
//    }

    class GroupHolder {
        public LinearLayout mRoot;
        public ImageView mIcon;
        public TextView mTitle;
        public TextView mIntro;
        public ImageView mArrow;
        public View mViewDivider;
        public ImageView mIndicator;
    }

    class ItemHolder {
        public FrameLayout mRoot;
        public TextView mText;
        public View mViewDivider;
    }

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GroupHolder groupHolder = null;
        if (null == convertView) {
            convertView = mLayoutInflater.inflate(R.layout.win_help_detail_group, null);
            groupHolder = new GroupHolder();
            groupHolder.mRoot = (LinearLayout) convertView.findViewById(R.id.help_group_layout);
            groupHolder.mArrow = (ImageView) convertView.findViewById(R.id.help_group_arrow);
            groupHolder.mTitle = (TextView) convertView.findViewById(R.id.help_group_title);
            groupHolder.mIntro = (TextView) convertView.findViewById(R.id.help_group_intro);
            groupHolder.mIcon = (ImageView) convertView.findViewById(R.id.help_group_icon);
            groupHolder.mViewDivider = (View) convertView.findViewById(R.id.help_group_divider);
            TextViewUtil.setTextSize(groupHolder.mTitle,ViewConfiger.SIZE_HELP_ITEM_SIZE1);
            TextViewUtil.setTextColor(groupHolder.mTitle,ViewConfiger.COLOR_HELP_ITEM_COLOR1);
            TextViewUtil.setTextSize(groupHolder.mIntro,ViewConfiger.SIZE_HELP_ITEM_SIZE2);
            TextViewUtil.setTextColor(groupHolder.mIntro,ViewConfiger.COLOR_HELP_ITEM_COLOR2);
            
            convertView.setTag(groupHolder);
            prepareSetLayoutParams(convertView);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }
        if (position == ConfigUtil.getVisbileCount() - 1) {
            	groupHolder.mViewDivider.setVisibility(View.GONE);
    		}else {
    			groupHolder.mViewDivider.setVisibility(View.VISIBLE);
    		}
        HelpDetail helpBean = ((HelpDetail)getItem(position));
        if (!WinManager.getInstance().hasThirdImpl()) {
			if (helpBean.isNew && HelpPreferenceUtil.getInstance().getBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_TAG, false)) {
				groupHolder.mTitle.setCompoundDrawablePadding((int) LayouUtil.getDimen("x10"));
				Drawable drawable = LayouUtil.getDrawable("ic_help_new");
				if (drawable != null) {
					int height = drawable.getIntrinsicHeight();
					float scale = (float) groupHolder.mTitle.getLineHeight()
							/ (float) height * 0.65f;
					drawable.setBounds(0, 0,
							(int) (drawable.getIntrinsicWidth() * scale),
							(int) (height * scale));
					groupHolder.mTitle.setCompoundDrawables(null, null, drawable, null);
				}
			}
		}
		if (isFromFile) {
			ImageLoader.getInstance().displayImage(
					"file://" + helpBean.iconName, new ImageViewAware(groupHolder.mIcon));
		} else {
			groupHolder.mIcon.setImageDrawable(LayouUtil.getDrawable(helpBean.iconName));
		}

		if (canOpenDetail && !WinManager.getInstance().hasThirdImpl()) {
			groupHolder.mArrow.setVisibility(View.VISIBLE);
		}else {
			groupHolder.mArrow.setVisibility(View.GONE);
		}

        groupHolder.mTitle.setText(LanguageConvertor.toLocale(""+helpBean.title));
        groupHolder.mIntro.setText(Html.fromHtml(LanguageConvertor.toLocale(helpBean.intro)));
        
		// if (position == mFocusIndex) {
		// convertView.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_focused));
		// } else {
		// convertView.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_nor));
		// }
        
        return convertView;
	}
}

