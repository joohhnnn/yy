package com.txznet.txz.ui.win.help;

import java.util.ArrayList;
import java.util.List;

import com.txznet.txz.R;
import com.txznet.txz.module.weixin.WeixinManager;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * get data from res,and add view to list.
 *
 */
public class WinHelpDetailAdapter implements ExpandableListAdapter {
	
	LayoutInflater mLayoutInflater;
	private Context mContext;
	
	List<HelpDetail> helpDetails;
	
	
	public WinHelpDetailAdapter(Context context) {
		mLayoutInflater = LayoutInflater.from(context);
		this.mContext = context;
		initHelpData(context);
	}
	
	public void initHelpData(Context context){
		helpDetails = new ArrayList<HelpDetail>();
		String[] titles = context.getResources().getStringArray(R.array.win_help_title);
		String[] intros = context.getResources().getStringArray(R.array.win_help_intro);
		String[] desps = context.getResources().getStringArray(R.array.win_help_desps);
		TypedArray array = context.getResources().obtainTypedArray(R.array.win_delp_icon);
		HelpDetail helpDetail;
		int size = WeixinManager.getInstance().isWeixinInstalled()?titles.length:titles.length-1;
		for(int i=0;i<size;i++){
			helpDetail = new HelpDetail();
			helpDetail.title = titles[i];
			helpDetail.intro = intros[i];
			helpDetail.iconResId = array.getResourceId(i, R.drawable.win_help_wechat);
			helpDetail.desps = desps[i].split("\n");
			helpDetails.add(helpDetail);
		}
		array.recycle();
	}
	


    @Override
    public int getGroupCount() {
        return helpDetails.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return helpDetails.get(groupPosition).desps.length;
    }

    @Override
    public HelpDetail getGroup(int groupPosition) {
        return helpDetails.get(groupPosition);
    }

    @Override
    public String getChild(int groupPosition, int childPosition) {
        return helpDetails.get(groupPosition).desps[childPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder groupHolder = null;
        if (null == convertView) {
            convertView = mLayoutInflater.inflate(R.layout.win_help_detail_group, null);
            groupHolder = new GroupHolder();
            groupHolder.mRoot = (FrameLayout) convertView.findViewById(R.id.help_group_layout);
            groupHolder.mArrow = (ImageView) convertView.findViewById(R.id.help_group_arrow);
            groupHolder.mTitle = (TextView) convertView.findViewById(R.id.help_group_title);
            groupHolder.mIntro = (TextView) convertView.findViewById(R.id.help_group_intro);
            groupHolder.mIcon = (ImageView) convertView.findViewById(R.id.help_group_icon);
            groupHolder.mViewDivider = (View) convertView.findViewById(R.id.help_group_divider);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }
        if (!isExpanded) {
            groupHolder.mRoot.setBackgroundResource(R.drawable.shape_help_group_back);
            groupHolder.mArrow.setRotation(0);
            groupHolder.mViewDivider.setVisibility(View.GONE);
            groupHolder.mIntro.setVisibility(View.VISIBLE);
        } else {
            groupHolder.mRoot.setBackgroundResource(R.drawable.shape_help_group_back_expanded);
            groupHolder.mArrow.setRotation(180);
            groupHolder.mViewDivider.setVisibility(View.VISIBLE);
            groupHolder.mIntro.setVisibility(View.INVISIBLE);
        }
        groupHolder.mIcon.setImageResource(getGroup(groupPosition).iconResId);
        groupHolder.mTitle.setText(""+getGroup(groupPosition).title);
        groupHolder.mIntro.setText(" - "+getGroup(groupPosition).intro);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ItemHolder holder = null;
        if (null == convertView) {
            convertView = mLayoutInflater.inflate(R.layout.win_help_detail_desp, null);
            holder = new ItemHolder();
            holder.mRoot = (FrameLayout) convertView.findViewById(R.id.help_detail_desp_layout);
            holder.mText = (TextView) convertView.findViewById(R.id.help_detail_desp);
            convertView.setTag(holder);
        } else {
            holder = (ItemHolder) convertView.getTag();
        }

        if(childPosition == 0){
            holder.mRoot.setPadding(0, (int) mContext.getResources().getDimension(R.dimen.y10), 0, 0);
        }

        if(isLastChild){
            holder.mRoot.setBackgroundResource(R.drawable.shape_help_item_back_last);
            holder.mRoot.setPadding(0, 0, 0, (int) mContext.getResources().getDimension(R.dimen.y10));
        }else{
            holder.mRoot.setBackgroundColor(0xff282828);
            holder.mRoot.setPadding(0, (int) mContext.getResources().getDimension(R.dimen.y10), 0, 0);
        }

        String str = getChild(groupPosition, childPosition);

        if(str.startsWith("“")){
            holder.mText.setGravity(Gravity.LEFT);
        }else{
            holder.mText.setGravity(Gravity.LEFT);
        }
        holder.mText.setText(str);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {

    }

    @Override
    public void onGroupCollapsed(int groupPosition) {

    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return 0;
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        return 0;
    }

    class GroupHolder {
        public FrameLayout mRoot;
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
    }

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}
}

