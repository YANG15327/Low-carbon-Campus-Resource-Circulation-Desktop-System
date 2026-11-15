package com.campus.recycle.ui;

import com.campus.recycle.model.Category;
import com.campus.recycle.model.Item;

import javax.swing.table.AbstractTableModel;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class ItemTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"物品名称", "分类", "价格/元", "状态", "卖家ID", "标签", "描述"};
    private List<Item> items = List.of();
    private Map<Integer, String> categoryNames = Map.of();

    void setItems(List<Item> items, List<Category> categories) {
        this.items = List.copyOf(items);
        Map<Integer, String> names = new HashMap<>();
        for (Category category : categories) {
            names.put(category.getId(), category.getName());
        }
        categoryNames = Map.copyOf(names);
        fireTableDataChanged();
    }

    Item getItemAt(int row) {
        return items.get(row);
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Item item = items.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> item.getName();
            case 1 -> categoryNames.getOrDefault(item.getCategoryId(), "未知分类");
            case 2 -> String.format(Locale.ROOT, "%.2f", item.getPrice());
            case 3 -> item.getStatus().getDisplayName();
            case 4 -> item.getSellerId();
            case 5 -> String.join(", ", item.getTags());
            case 6 -> item.getDescription() == null ? "" : item.getDescription();
            default -> throw new IndexOutOfBoundsException("未知列: " + columnIndex);
        };
    }
}
