# ShoppingCarBestImplementation
Android 购物车的最佳实现（通过ExpandableListView实现分店铺），主要功能包括：单选、全选、合计、删除、商品数量的加减，为了方便测试又新增了刷新数据功能。
项目博客地址：https://blog.csdn.net/qq941263013/article/details/80901277
个人博客地址：https://blog.csdn.net/qq941263013
GIF效果图：
![img]https://github.com/wangyang0313/ShoppingCarBestImplementation/blob/master/app/gif.gif

简单说明（详细描述见博客、源码）：
    1.页面布局文件：实际开发中，如果遇到ScrollView嵌套ExpandableListView控件，可以使用自定义控件ExpandableListViewForScrollView解决ScrollView嵌套ExpandableListView，ExpandableListView显示不全的问题。（地址：https://blog.csdn.net/qq941263013/article/details/80655909）
    2.初始化数据（含Bean类）：在bean类的DatasBean中新增了isSelect_shop属性，用来判断店铺是否被选中；在GoodsBean中新增了isSelect属性，用来判断商品是否被选中。
    3.初始化ExpandableListView：使用的是ExpandableListView，所以adapter继承BaseExpandableListAdapter，并实现其未实现的方法。
    4.初始化ExpandableListView的数据：初始化了ExpandableListView的数据，并保证数据刷新时，页面保持当前位置。同时也使ExpandableListView的所有组展开，并点击无效果。
