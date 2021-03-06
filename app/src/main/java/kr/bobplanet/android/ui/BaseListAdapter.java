package kr.bobplanet.android.ui;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;

import java.util.List;

import kr.bobplanet.android.App;

/**
 * 화면마다 별로 할일도 없는 Adapter들이 많아지는 것 같아 한단계 추상화한 클래스.
 *
 * @author heonkyu.jin
 * @version 15. 10. 17
 */
public class BaseListAdapter<T> extends RecyclerView.Adapter<BaseListAdapter.BaseViewHolder> {
    final List<T> itemList;
    final BaseViewHolderFactory factory;
    @LayoutRes final int layoutResId;

    public BaseListAdapter(BaseViewHolderFactory factory, List itemList, @LayoutRes int layoutResId) {
        this.factory = factory;
        this.itemList = itemList;
        this.layoutResId = layoutResId;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
        return factory.newInstance(view);
    }

    @Override
    final public void onBindViewHolder(BaseViewHolder holder, int position) {
        Object item = get(position);
        holder.setItem(item);
    }

    public void add(int position, T item) {
        itemList.add(position, item);
    }

    public void remove(int position) {
        itemList.remove(position);
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    public T get(int location) {
        return itemList == null ? null : itemList.get(location);
    }

    /**
     * BaseListAdpater를 사용하려면 이 클래스를 상속받은 ViewHolder 클래스에서 setItem()을 구현해주면 됨.
     *
     * @param <T>
     */
    protected abstract static class BaseViewHolder<T> extends RecyclerView.ViewHolder {
        private final ImageLoader imageLoader = App.getImageLoader();

        BaseViewHolder(View view) {
            super(view);
        }

        protected ImageLoader getImageLoader() {
            return imageLoader;
        }

        abstract void setItem(T item);
    }

    protected interface BaseViewHolderFactory {
        BaseViewHolder newInstance(View view);
    }
}
