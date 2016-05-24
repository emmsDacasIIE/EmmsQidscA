package cn.dacas.emmclient.contacts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.haarman.listviewanimations.itemmanipulation.ExpandableListItemAdapter;

import java.util.List;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.main.EmmClientApplication;

public class ExpandableAdapter extends ExpandableListItemAdapter<SortModel>
		implements SectionIndexer {
	private Context mContext;

	public ExpandableAdapter(Context mContext, List<SortModel> list) {
		super(mContext, R.layout.activity_listviews_expandablelistitem_card,
                R.id.activity_expandablelistitem_card_title,
                R.id.activity_expandablelistitem_card_content, list);
		this.mContext = mContext;
	}

	@Override
	public View getTitleView(int position, View view, ViewGroup parent) {
		ViewHolderTitle viewHolder = null;
		final SortModel mContent = getItem(position);
		if (view == null) {
			viewHolder = new ViewHolderTitle();
			view = LayoutInflater.from(mContext).inflate(
					R.layout.activity_listviews_expandablelistitem_card_title,
					null);
			viewHolder.catalog = (TextView) view.findViewById(R.id.catalog);
			viewHolder.contactIconImageView = (ImageView) view
					.findViewById(R.id.contactIconImageView);
			viewHolder.contactNameTextView = (TextView) view
					.findViewById(R.id.contactNameTextView);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolderTitle) view.getTag();
		}

		int section = getSectionForPosition(position);

		if (position == getPositionForSection(section)) {
			viewHolder.catalog.setVisibility(View.VISIBLE);
			viewHolder.catalog.setText(mContent.getSortLetters());
		} else {
			viewHolder.catalog.setVisibility(View.GONE);
		}

		viewHolder.contactNameTextView.setText(mContent.getContactName());

		view.setClickable(false);
		return view;
	}

	@Override
	public View getContentView(int position, View view, ViewGroup parent) {
		ViewHolderContent viewHolder = null;
		final SortModel mContent = getItem(position);
		if (view == null) {
			viewHolder = new ViewHolderContent();
			view = LayoutInflater
					.from(mContext)
					.inflate(
							R.layout.activity_listviews_expandablelistitem_card_content,
							null);
			viewHolder.contactTelePhone = (LinearLayout)view.findViewById(R.id.contactTelephone);
			viewHolder.contactTelephoneLabel = (TextView) view
					.findViewById(R.id.contactTelephoneTextView);
			viewHolder.contactTelephoneTextView = (TextView) view
					.findViewById(R.id.contactTelephoneTextView);
			viewHolder.contactTelephoneCallImageView = (ImageView)view.findViewById(R.id.contactTelephoneCallImageView);
			
			viewHolder.contactPhone1 = (LinearLayout)view.findViewById(R.id.contactPhone1);
			viewHolder.contactPhone1Label = (TextView) view
					.findViewById(R.id.contactPhone1TextView);
			viewHolder.contactPhone1TextView = (TextView) view
					.findViewById(R.id.contactPhone1TextView);
			viewHolder.contactPhone1CallImageView = (ImageView)view.findViewById(R.id.contactPhone1CallImageView);
			viewHolder.contactPhone1MsgImageView = (ImageView)view.findViewById(R.id.contactPhone1MsgImageView);
			
			viewHolder.contactPhone2 = (LinearLayout)view.findViewById(R.id.contactPhone2);
			viewHolder.contactPhone2Label = (TextView) view
					.findViewById(R.id.contactPhone2TextView);
			viewHolder.contactPhone2TextView = (TextView) view
					.findViewById(R.id.contactPhone2TextView);
			viewHolder.contactPhone2CallImageView = (ImageView)view.findViewById(R.id.contactPhone2CallImageView);
			viewHolder.contactPhone2MsgImageView = (ImageView)view.findViewById(R.id.contactPhone2MsgImageView);
			
			viewHolder.contactEmail1 = (LinearLayout)view.findViewById(R.id.contactEmail1);
			viewHolder.contactEmail1Label = (TextView) view
					.findViewById(R.id.contactEmail1TextView);
			viewHolder.contactEmail1TextView = (TextView) view
					.findViewById(R.id.contactEmail1TextView);
			viewHolder.contactEmail1MsgImageView = (ImageView)view.findViewById(R.id.contactEmail1MsgImageView);
			
			viewHolder.contactEmail2 = (LinearLayout)view.findViewById(R.id.contactEmail2);
			viewHolder.contactEmail2Label = (TextView) view
					.findViewById(R.id.contactEmail2TextView);
			viewHolder.contactEmail2TextView = (TextView) view
					.findViewById(R.id.contactEmail2TextView);
			viewHolder.contactEmail2MsgImageView = (ImageView)view.findViewById(R.id.contactEmail2MsgImageView);
			
			viewHolder.contactCompany = (LinearLayout)view.findViewById(R.id.contactCompany);
			viewHolder.contactCompanyLabel = (TextView) view
					.findViewById(R.id.contactCompanyTextView);
			viewHolder.contactCompanyTextView = (TextView) view
					.findViewById(R.id.contactCompanyTextView);
			
			viewHolder.contactAddress = (LinearLayout)view.findViewById(R.id.contactAddress);
			viewHolder.contactAddressLabel = (TextView) view
					.findViewById(R.id.contactAddressTextView);
			viewHolder.contactAddressTextView = (TextView) view
					.findViewById(R.id.contactAddressTextView);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolderContent) view.getTag();
		}

		if (mContent.getTelephone() != null
				&& !mContent.getTelephone().equals("")) {
			viewHolder.contactTelephoneTextView
					.setText(mContent.getTelephone());
			viewHolder.contactTelephoneCallImageView
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Uri uri = Uri.parse("tel:"
									+ mContent.getTelephone());
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_CALL);
							intent.setData(uri);
							v.getContext().startActivity(intent);
						}
					});
			viewHolder.contactTelePhone.setVisibility(View.VISIBLE);
		} else {
			viewHolder.contactTelePhone.setVisibility(View.GONE);
		}

		if (mContent.getCellphone_1() != null
				&& !mContent.getCellphone_1().equals("")) {
			viewHolder.contactPhone1TextView.setText(mContent.getCellphone_1());
			viewHolder.contactPhone1CallImageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("tel:" + mContent.getCellphone_1());
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_CALL);
                    intent.setData(uri);
                    v.getContext().startActivity(intent);
                }
            });
			viewHolder.contactPhone1MsgImageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri
                            .parse("smsto:" + mContent.getCellphone_1()));
                    v.getContext().startActivity(intent);
                }
            });
			viewHolder.contactPhone1.setVisibility(View.VISIBLE);
		} else {
			viewHolder.contactPhone1.setVisibility(View.GONE);
		}

		if (mContent.getCellphone_2() != null
				&& !mContent.getCellphone_2().equals("")) {
			viewHolder.contactPhone2TextView.setText(mContent.getCellphone_2());
			viewHolder.contactPhone2CallImageView
					.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Uri uri = Uri.parse("tel:"
                                    + mContent.getCellphone_2());
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_CALL);
                            intent.setData(uri);
                            v.getContext().startActivity(intent);
                        }
                    });
			viewHolder.contactPhone2MsgImageView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_SENDTO, Uri
							.parse("smsto:" + mContent.getCellphone_2()));
					v.getContext().startActivity(intent);
				}
			});
			viewHolder.contactPhone2.setVisibility(View.VISIBLE);
		} else {
			viewHolder.contactPhone2.setVisibility(View.GONE);
		}

		if (mContent.getEmail_1() != null && !mContent.getEmail_1().equals("")) {
			viewHolder.contactEmail1TextView.setText(mContent.getEmail_1());
			viewHolder.contactEmail1MsgImageView
					.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            String[] reciver = new String[]{mContent.getEmail_1()};
                            Intent myIntent = new Intent(android.content.Intent.ACTION_SEND);
                            myIntent.setType("plain/text");
                            myIntent.putExtra(android.content.Intent.EXTRA_EMAIL, reciver);
                            v.getContext().startActivity(Intent.createChooser(myIntent, "给" + mContent.getContactName() + "发送邮件"));
                        }
                    });
			viewHolder.contactEmail1.setVisibility(View.VISIBLE);
		} else {
			viewHolder.contactEmail1.setVisibility(View.GONE);
		}

		if (mContent.getEmail_2() != null && !mContent.getEmail_2().equals("")) {
			viewHolder.contactEmail2TextView.setText(mContent.getEmail_2());
			viewHolder.contactEmail2MsgImageView
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							String[] reciver = new String[] { mContent.getEmail_2() };
							Intent myIntent = new Intent(android.content.Intent.ACTION_SEND);
							myIntent.setType("plain/text");
							myIntent.putExtra(android.content.Intent.EXTRA_EMAIL, reciver);
							v.getContext().startActivity(Intent.createChooser(myIntent,	"给"+mContent.getContactName() + "发送邮件"));
						}
					});
			viewHolder.contactEmail2.setVisibility(View.VISIBLE);
		} else {
			viewHolder.contactEmail2.setVisibility(View.GONE);
		}

		if (mContent.getContactCompany() != null
				&& !mContent.getContactCompany().equals("")) {
			viewHolder.contactCompanyTextView.setText(mContent
                    .getContactCompany());
			
			viewHolder.contactCompany.setVisibility(View.VISIBLE);
		} else {
			viewHolder.contactCompany.setVisibility(View.GONE);
		}

		if (mContent.getContactAddress() != null
				&& !mContent.getContactAddress().equals("")) {
			viewHolder.contactAddressTextView.setText(mContent
					.getContactAddress());
			viewHolder.contactAddress.setVisibility(View.VISIBLE);
		} else {
			viewHolder.contactAddress.setVisibility(View.GONE);
		}
		if (EmmClientApplication.mPhoneInfo.getIMSI()==null) {
            viewHolder.contactPhone1CallImageView.setVisibility(View.INVISIBLE);
            viewHolder.contactPhone2CallImageView.setVisibility(View.INVISIBLE);
            viewHolder.contactPhone1MsgImageView.setVisibility(View.INVISIBLE);
            viewHolder.contactPhone2MsgImageView.setVisibility(View.INVISIBLE);
        }
		return view;
	}

	public void updateListView(List<SortModel> list) {
		this.clear();
		this.addAll(list);
		notifyDataSetChanged();
	}

	final static class ViewHolderTitle {
		TextView catalog;
		ImageView contactIconImageView;
		TextView contactNameTextView;
		// ImageView contactMsgImageView;
		// ImageView contactDialImageView;
	}

	final static class ViewHolderContent {
		LinearLayout contactTelePhone;
		TextView contactTelephoneLabel;
		TextView contactTelephoneTextView;
		ImageView contactTelephoneCallImageView;
		
		LinearLayout contactPhone1;
		TextView contactPhone1Label;
		TextView contactPhone1TextView;
		ImageView contactPhone1CallImageView;
		ImageView contactPhone1MsgImageView;
		
		LinearLayout contactPhone2;
		TextView contactPhone2Label;
		TextView contactPhone2TextView;
		ImageView contactPhone2CallImageView;
		ImageView contactPhone2MsgImageView;
		
		LinearLayout contactEmail1;
		TextView contactEmail1Label;
		TextView contactEmail1TextView;
		ImageView contactEmail1MsgImageView;
		
		LinearLayout contactEmail2;
		TextView contactEmail2Label;
		TextView contactEmail2TextView;
		ImageView contactEmail2MsgImageView;
		
		LinearLayout contactCompany;
		TextView contactCompanyLabel;
		TextView contactCompanyTextView;
		
		LinearLayout contactAddress;
		TextView contactAddressLabel;
		TextView contactAddressTextView;	
	}

	public int getSectionForPosition(int position) {
		return getItem(position).getSortLetters().charAt(0);
	}

	@SuppressLint("DefaultLocale")
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = getItem(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}

		return -1;
	}

	// private String getAlpha(String str) {
	// String sortStr = str.trim().substring(0, 1).toUpperCase();
	// if (sortStr.matches("[A-Z]")) {
	// return sortStr;
	// } else {
	// return "#";
	// }
	// }

	@Override
	public Object[] getSections() {
		return null;
	}

}