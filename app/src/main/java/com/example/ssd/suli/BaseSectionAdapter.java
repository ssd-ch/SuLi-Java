package com.example.ssd.shimaneuniversitybrowser;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BaseSectionAdapter<T1, T2> extends BaseAdapter {

    /** インデックス行:ヘッダー */
    private static final int INDEX_PATH_ROW_HEADER = -1;

    /** ビュータイプ:ヘッダー行 */
    private static final int ITEM_VIEW_TYPE_HEADER = 0;

    /** ビュータイプ:データ行 */
    private static final int ITEM_VIEW_TYPE_ROW = 1;

    protected Context context;
    protected LayoutInflater inflater;

    /** ヘッダー行で使用するデータリスト */
    protected List<T1> sectionList;

    /** データ行で使用するデータリスト */
    protected List<List<T2>> rowList;

    private List<IndexPath> indexPathList;

    public BaseSectionAdapter(Context context, List<T1> sectionList, List<List<T2>> rowList) {
        super();
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.sectionList = sectionList;
        this.rowList = rowList;
        this.indexPathList = getIndexPathList(sectionList, rowList);
    }

    @Override
    public int getCount() {
        int count = indexPathList.size();
        return count;
    }

    @Override
    public Object getItem(int position) {
        IndexPath indexPath = indexPathList.get(position);
        if (isHeader(indexPath)) {
            return sectionList.get(indexPath.section);
        } else {
            return rowList.get(indexPath.section).get(indexPath.row);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        IndexPath indexPath = indexPathList.get(position);

        // ヘッダー行とデータ行とで分岐します。
        if (isHeader(indexPath)) {
            return viewForHeaderInSection(convertView, indexPath.section);
        } else {
            return cellForRowAtIndexPath(convertView, indexPath);
        }
    }

    /**
     * ヘッダー行のViewを返します。
     *
     * @param convertView
     * @param section
     * @return ヘッダー行のView
     */
    public View viewForHeaderInSection(View convertView, int section) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
            TextView castedConvertView = (TextView) convertView;
            castedConvertView.setBackgroundColor(Color.GRAY);
            castedConvertView.setTextColor(Color.WHITE);
        }
        TextView textView = (TextView) convertView;
        textView.setText(sectionList.get(section).toString());
        return convertView;
    }

    /**
     * データ行のViewを返します。
     *
     * @param convertView
     * @param indexPath
     * @return データ行のView
     */
    public View cellForRowAtIndexPath(View convertView, IndexPath indexPath) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }
        TextView textView = (TextView) convertView;
        textView.setText(rowList.get(indexPath.section).get(indexPath.row).toString());
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        // ヘッダー行とデータ行の2種類なので、2を返します。
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        // ビュータイプを返します。
        if (isHeader(position)) {
            return ITEM_VIEW_TYPE_HEADER;
        } else {
            return ITEM_VIEW_TYPE_ROW;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        if (isHeader(position)) {
            // ヘッダー行の場合は、タップできないようにします。
            return false;
        } else {
            return super.isEnabled(position);
        }
    }

    /**
     * インデックスパスリストを取得します。
     *
     * @param sectionList
     * @param rowList
     * @return インデックスパスリスト
     */
    private List<IndexPath> getIndexPathList(List<T1> sectionList, List<List<T2>> rowList) {
        List<IndexPath> indexPathList = new ArrayList<IndexPath>();
        for (int i = 0; i < sectionList.size(); i++) {
            IndexPath sectionIndexPath = new IndexPath();
            sectionIndexPath.section = i;
            sectionIndexPath.row = INDEX_PATH_ROW_HEADER;
            indexPathList.add(sectionIndexPath);

            List<T2> rowListBySection = rowList.get(i);
            for (int j = 0; j < rowListBySection.size(); j++) {
                IndexPath rowIndexPath = new IndexPath();
                rowIndexPath.section = i;
                rowIndexPath.row = j;
                indexPathList.add(rowIndexPath);
            }
        }
        return indexPathList;
    }

    private boolean isHeader(int position) {
        IndexPath indexPath = indexPathList.get(position);
        return isHeader(indexPath);
    }

    private boolean isHeader(IndexPath indexPath) {
        if (INDEX_PATH_ROW_HEADER == indexPath.row) {
            return true;
        } else {
            return false;
        }
    }
}
