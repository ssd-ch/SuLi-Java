package cf.ssdb.suli;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class CustomSectionListAdapter extends BaseSectionAdapter<SectionHeaderData, SectionRowData> {

    public CustomSectionListAdapter(Context context, List<SectionHeaderData> sectionList, List<List<SectionRowData>> rowList) {
        super(context, sectionList, rowList);
    }

    @Override
    public View viewForHeaderInSection(View convertView, int section) {
        ListHeaderViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_header, null);
            holder = new ListHeaderViewHolder();
            holder.titleTxt = (TextView) convertView.findViewById(R.id.titleTxt);
            holder.subtitleTxt = (TextView) convertView.findViewById(R.id.subtitleTxt);
            convertView.setTag(holder);
        } else {
            holder = (ListHeaderViewHolder) convertView.getTag();
        }
        SectionHeaderData headerData = sectionList.get(section);
        holder.titleTxt.setText(headerData.title);
        holder.subtitleTxt.setText(headerData.subTitle);
        return convertView;
    }

    @Override
    public View cellForRowAtIndexPath(View convertView, IndexPath indexPath) {
        ListRowViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row, null);
            holder = new ListRowViewHolder();
            holder.labelTxt = (TextView) convertView.findViewById(R.id.labelTxt);
            holder.valueTxt = (TextView) convertView.findViewById(R.id.valueTxt);
            convertView.setTag(holder);
        } else {
            holder = (ListRowViewHolder) convertView.getTag();
        }
        SectionRowData rowData = rowList.get(indexPath.section).get(indexPath.row);
        if (rowData.color.equals(""))
            rowData.color = "#000000";
        holder.labelTxt.setText(rowData.label);
        holder.labelTxt.setTextColor(Color.parseColor(rowData.color));
        holder.valueTxt.setText(rowData.value);
        return convertView;
    }

    class ListHeaderViewHolder {
        TextView titleTxt;
        TextView subtitleTxt;
    }

    class ListRowViewHolder {
        TextView labelTxt;
        TextView valueTxt;
    }
}