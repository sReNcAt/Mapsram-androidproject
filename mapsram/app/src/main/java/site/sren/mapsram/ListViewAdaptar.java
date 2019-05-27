package site.sren.mapsram;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdaptar extends BaseAdapter {
    // 아이템 데이터 리스트.
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }

    // position 위치의 아이템 타입 리턴.
    @Override
    public int getItemViewType(int position) {
        return listViewItemList.get(position).getAlramType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();
        int viewType = getItemViewType(position) ;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
            // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
            ListViewItem listViewItem = listViewItemList.get(position);

        }

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    //아이템 추가를 위한 함수.
    public void addItem(String work, String memo,int alaramType,int hour, int minutes ,Double latitude, Double logitude ) {
        ListViewItem item = new ListViewItem() ;

        item.setWork(work) ;
        item.setMemo(memo) ;
        item.setAlramType(alaramType);
        item.setHour(hour);
        item.setMinutes(minutes);
        item.setLatitude(latitude);
        item.setLogitude(logitude);

        listViewItemList.add(item) ;
    }
}