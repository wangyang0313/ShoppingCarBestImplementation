package com.wangyang.shoppingcarbestimplementation.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wangyang.shoppingcarbestimplementation.R;
import com.wangyang.shoppingcarbestimplementation.bean.ShoppingCarDataBean;
import com.wangyang.shoppingcarbestimplementation.util.ToastUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 购物车的adapter
 * 因为使用的是ExpandableListView，所以继承BaseExpandableListAdapter
 */
public class ShoppingCarAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final LinearLayout llSelectAll;
    private final ImageView ivSelectAll;
    private final Button btnOrder;
    private final Button btnDelete;
    private final RelativeLayout rlTotalPrice;
    private final TextView tvTotalPrice;
    private List<ShoppingCarDataBean.DatasBean> data;
    private boolean isSelectAll = false;
    private double total_price;

    public ShoppingCarAdapter(Context context, LinearLayout llSelectAll,
                              ImageView ivSelectAll, Button btnOrder, Button btnDelete,
                              RelativeLayout rlTotalPrice, TextView tvTotalPrice) {
        this.context = context;
        this.llSelectAll = llSelectAll;
        this.ivSelectAll = ivSelectAll;
        this.btnOrder = btnOrder;
        this.btnDelete = btnDelete;
        this.rlTotalPrice = rlTotalPrice;
        this.tvTotalPrice = tvTotalPrice;
    }

    /**
     * 自定义设置数据方法；
     * 通过notifyDataSetChanged()刷新数据，可保持当前位置
     *
     * @param data 需要刷新的数据
     */
    public void setData(List<ShoppingCarDataBean.DatasBean> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        if (data != null && data.size() > 0) {
            return data.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return data.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_shopping_car_group, null);

            groupViewHolder = new GroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        final ShoppingCarDataBean.DatasBean datasBean = data.get(groupPosition);
        //店铺ID
        String store_id = datasBean.getStore_id();
        //店铺名称
        String store_name = datasBean.getStore_name();

        if (store_name != null) {
            groupViewHolder.tvStoreName.setText(store_name);
        } else {
            groupViewHolder.tvStoreName.setText("");
        }

        //店铺内的商品都选中的时候，店铺的也要选中
        for (int i = 0; i < datasBean.getGoods().size(); i++) {
            ShoppingCarDataBean.DatasBean.GoodsBean goodsBean = datasBean.getGoods().get(i);
            boolean isSelect = goodsBean.getIsSelect();
            if (isSelect) {
                datasBean.setIsSelect_shop(true);
            } else {
                datasBean.setIsSelect_shop(false);
                break;
            }
        }

        //因为set之后要重新get，所以这一块代码要放到一起执行
        //店铺是否在购物车中被选中
        final boolean isSelect_shop = datasBean.getIsSelect_shop();
        if (isSelect_shop) {
            groupViewHolder.ivSelect.setImageResource(R.mipmap.select);
        } else {
            groupViewHolder.ivSelect.setImageResource(R.mipmap.unselect);
        }

        //店铺选择框的点击事件
        groupViewHolder.ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datasBean.setIsSelect_shop(!isSelect_shop);

                List<ShoppingCarDataBean.DatasBean.GoodsBean> goods = datasBean.getGoods();
                for (int i = 0; i < goods.size(); i++) {
                    ShoppingCarDataBean.DatasBean.GoodsBean goodsBean = goods.get(i);
                    goodsBean.setIsSelect(!isSelect_shop);
                }
                notifyDataSetChanged();
            }
        });

        //当所有的选择框都是选中的时候，全选也要选中
        w:
        for (int i = 0; i < data.size(); i++) {
            List<ShoppingCarDataBean.DatasBean.GoodsBean> goods = data.get(i).getGoods();
            for (int y = 0; y < goods.size(); y++) {
                ShoppingCarDataBean.DatasBean.GoodsBean goodsBean = goods.get(y);
                boolean isSelect = goodsBean.getIsSelect();
                if (isSelect) {
                    isSelectAll = true;
                } else {
                    isSelectAll = false;
                    break w;//根据标记，跳出嵌套循环
                }
            }
        }
        if (isSelectAll) {
            ivSelectAll.setBackgroundResource(R.mipmap.select);
        } else {
            ivSelectAll.setBackgroundResource(R.mipmap.unselect);
        }

        //全选的点击事件
        llSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSelectAll = !isSelectAll;

                if (isSelectAll) {
                    for (int i = 0; i < data.size(); i++) {
                        List<ShoppingCarDataBean.DatasBean.GoodsBean> goods = data.get(i).getGoods();
                        for (int y = 0; y < goods.size(); y++) {
                            ShoppingCarDataBean.DatasBean.GoodsBean goodsBean = goods.get(y);
                            goodsBean.setIsSelect(true);
                        }
                    }
                } else {
                    for (int i = 0; i < data.size(); i++) {
                        List<ShoppingCarDataBean.DatasBean.GoodsBean> goods = data.get(i).getGoods();
                        for (int y = 0; y < goods.size(); y++) {
                            ShoppingCarDataBean.DatasBean.GoodsBean goodsBean = goods.get(y);
                            goodsBean.setIsSelect(false);
                        }
                    }
                }
                notifyDataSetChanged();
            }
        });

        //合计的计算
        total_price = 0.0;
        tvTotalPrice.setText("¥0.00");
        for (int i = 0; i < data.size(); i++) {
            List<ShoppingCarDataBean.DatasBean.GoodsBean> goods = data.get(i).getGoods();
            for (int y = 0; y < goods.size(); y++) {
                ShoppingCarDataBean.DatasBean.GoodsBean goodsBean = goods.get(y);
                boolean isSelect = goodsBean.getIsSelect();
                if (isSelect) {
                    String num = goodsBean.getGoods_num();
                    String price = goodsBean.getGoods_price();

                    double v = Double.parseDouble(num);
                    double v1 = Double.parseDouble(price);

                    total_price = total_price + v * v1;

                    //让Double类型完整显示，不用科学计数法显示大写字母E
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    tvTotalPrice.setText("¥" + decimalFormat.format(total_price));
                }
            }
        }

        //去结算的点击事件
        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建临时的List，用于存储被选中的商品
                List<ShoppingCarDataBean.DatasBean.GoodsBean> temp = new ArrayList<>();
                for (int i = 0; i < data.size(); i++) {
                    List<ShoppingCarDataBean.DatasBean.GoodsBean> goods = data.get(i).getGoods();
                    for (int y = 0; y < goods.size(); y++) {
                        ShoppingCarDataBean.DatasBean.GoodsBean goodsBean = goods.get(y);
                        boolean isSelect = goodsBean.getIsSelect();
                        if (isSelect) {
                            temp.add(goodsBean);
                        }
                    }
                }

                if (temp != null && temp.size() > 0) {//如果有被选中的
                    /**
                     * 实际开发中，如果有被选中的商品，
                     * 则跳转到确认订单页面，完成后续订单流程。
                     */
                    ToastUtil.makeText(context, "跳转到确认订单页面，完成后续订单流程");
                } else {
                    ToastUtil.makeText(context, "请选择要购买的商品");
                }
            }
        });

        //删除的点击事件
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 实际开发中，通过回调请求后台接口实现删除操作
                 */
                if (mDeleteListener != null) {
                    mDeleteListener.onDelete();
                }
            }
        });

        return convertView;
    }

    static class GroupViewHolder {
        @InjectView(R.id.iv_select)
        ImageView ivSelect;
        @InjectView(R.id.tv_store_name)
        TextView tvStoreName;
        @InjectView(R.id.ll)
        LinearLayout ll;

        GroupViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    //------------------------------------------------------------------------------------------------
    @Override
    public int getChildrenCount(int groupPosition) {
        if (data.get(groupPosition).getGoods() != null && data.get(groupPosition).getGoods().size() > 0) {
            return data.get(groupPosition).getGoods().size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return data.get(groupPosition).getGoods().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder childViewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_shopping_car_child, null);

            childViewHolder = new ChildViewHolder(convertView);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        final ShoppingCarDataBean.DatasBean datasBean = data.get(groupPosition);
        //店铺ID
        String store_id = datasBean.getStore_id();
        //店铺名称
        String store_name = datasBean.getStore_name();
        //店铺是否在购物车中被选中
        final boolean isSelect_shop = datasBean.getIsSelect_shop();
        final ShoppingCarDataBean.DatasBean.GoodsBean goodsBean = datasBean.getGoods().get(childPosition);
        //商品图片
        String goods_image = goodsBean.getGoods_image();
        //商品ID
        final String goods_id = goodsBean.getGoods_id();
        //商品名称
        String goods_name = goodsBean.getGoods_name();
        //商品价格
        String goods_price = goodsBean.getGoods_price();
        //商品数量
        String goods_num = goodsBean.getGoods_num();
        //商品是否被选中
        final boolean isSelect = goodsBean.getIsSelect();

        Glide.with(context)
                .load(goods_image)
                .into(childViewHolder.ivPhoto);
        if (goods_name != null) {
            childViewHolder.tvName.setText(goods_name);
        } else {
            childViewHolder.tvName.setText("");
        }
        if (goods_price != null) {
            childViewHolder.tvPriceValue.setText(goods_price);
        } else {
            childViewHolder.tvPriceValue.setText("");
        }
        if (goods_num != null) {
            childViewHolder.tvEditBuyNumber.setText(goods_num);
        } else {
            childViewHolder.tvEditBuyNumber.setText("");
        }

        //商品是否被选中
        if (isSelect) {
            childViewHolder.ivSelect.setImageResource(R.mipmap.select);
        } else {
            childViewHolder.ivSelect.setImageResource(R.mipmap.unselect);
        }

        //商品选择框的点击事件
        childViewHolder.ivSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goodsBean.setIsSelect(!isSelect);
                if (!isSelect == false) {
                    datasBean.setIsSelect_shop(false);
                }
                notifyDataSetChanged();
            }
        });

        //加号的点击事件
        childViewHolder.ivEditAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //模拟加号操作
                String num = goodsBean.getGoods_num();
                Integer integer = Integer.valueOf(num);
                integer++;
                goodsBean.setGoods_num(integer + "");
                notifyDataSetChanged();

                /**
                 * 实际开发中，通过回调请求后台接口实现数量的加减
                 */
                if (mChangeCountListener != null) {
                    mChangeCountListener.onChangeCount(goods_id);
                }
            }
        });
        //减号的点击事件
        childViewHolder.ivEditSubtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //模拟减号操作
                String num = goodsBean.getGoods_num();
                Integer integer = Integer.valueOf(num);
                if (integer > 1) {
                    integer--;
                    goodsBean.setGoods_num(integer + "");

                    /**
                     * 实际开发中，通过回调请求后台接口实现数量的加减
                     */
                    if (mChangeCountListener != null) {
                        mChangeCountListener.onChangeCount(goods_id);
                    }
                } else {
                    ToastUtil.makeText(context, "商品不能再减少了");
                }
                notifyDataSetChanged();
            }
        });

        if (childPosition == data.get(groupPosition).getGoods().size() - 1) {
            childViewHolder.view.setVisibility(View.GONE);
            childViewHolder.viewLast.setVisibility(View.VISIBLE);
        } else {
            childViewHolder.view.setVisibility(View.VISIBLE);
            childViewHolder.viewLast.setVisibility(View.GONE);
        }

        return convertView;
    }

    static class ChildViewHolder {
        @InjectView(R.id.iv_select)
        ImageView ivSelect;
        @InjectView(R.id.iv_photo)
        ImageView ivPhoto;
        @InjectView(R.id.tv_name)
        TextView tvName;
        @InjectView(R.id.tv_price_key)
        TextView tvPriceKey;
        @InjectView(R.id.tv_price_value)
        TextView tvPriceValue;
        @InjectView(R.id.iv_edit_subtract)
        ImageView ivEditSubtract;
        @InjectView(R.id.tv_edit_buy_number)
        TextView tvEditBuyNumber;
        @InjectView(R.id.iv_edit_add)
        ImageView ivEditAdd;
        @InjectView(R.id.view)
        View view;
        @InjectView(R.id.view_last)
        View viewLast;

        ChildViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    //-----------------------------------------------------------------------------------------------

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    //删除的回调
    public interface OnDeleteListener {
        void onDelete();
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        mDeleteListener = listener;
    }

    private OnDeleteListener mDeleteListener;

    //修改商品数量的回调
    public interface OnChangeCountListener {
        void onChangeCount(String goods_id);
    }

    public void setOnChangeCountListener(OnChangeCountListener listener) {
        mChangeCountListener = listener;
    }

    private OnChangeCountListener mChangeCountListener;
}
