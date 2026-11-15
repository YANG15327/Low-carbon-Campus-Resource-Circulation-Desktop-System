package com.campus.recycle.ui;

import com.campus.recycle.exception.BusinessException;
import com.campus.recycle.model.Category;
import com.campus.recycle.model.Item;
import com.campus.recycle.service.ItemService;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.List;

final class ItemDialog {

    private final ItemService itemService;
    private final int userId;
    private final Item item;
    private final JComboBox<Category> categoryField;
    private final JTextField nameField = new JTextField(24);
    private final JTextField priceField = new JTextField(24);
    private final JTextField tagsField = new JTextField(24);
    private final JTextArea descriptionField = new JTextArea(5, 24);

    ItemDialog(MainFrame owner, ItemService itemService, int userId,
               List<Category> categories, Item item) {
        this.itemService = itemService;
        this.userId = userId;
        this.item = item;
        categoryField = new JComboBox<>(categories.toArray(Category[]::new));
        categoryField.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value == null ? "" : value.getName());
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });

        JPanel fields = new JPanel(new GridLayout(5, 2, 8, 8));
        fields.add(new JLabel("物品名称"));
        fields.add(nameField);
        fields.add(new JLabel("分类"));
        fields.add(categoryField);
        fields.add(new JLabel("价格（元，0 表示交换）"));
        fields.add(priceField);
        fields.add(new JLabel("标签（逗号分隔）"));
        fields.add(tagsField);
        fields.add(new JLabel("描述（最多 500 字）"));
        fields.add(new JScrollPane(descriptionField));

        if (item != null) {
            nameField.setText(item.getName());
            categoryField.setSelectedItem(categories.stream()
                    .filter(category -> category.getId().equals(item.getCategoryId()))
                    .findFirst().orElse(null));
            priceField.setText(String.valueOf(item.getPrice()));
            tagsField.setText(String.join(", ", item.getTags()));
            descriptionField.setText(item.getDescription());
        }

        int result = JOptionPane.showConfirmDialog(owner, fields,
                item == null ? "填写物品信息" : "修改物品信息",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            save(owner);
        }
    }

    private void save(MainFrame owner) {
        try {
            Category category = (Category) categoryField.getSelectedItem();
            if (category == null) {
                throw new IllegalArgumentException("请选择物品分类");
            }
            double price = Double.parseDouble(priceField.getText().trim());
            String description = descriptionField.getText().trim();
            List<String> tags = Arrays.stream(tagsField.getText().split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .toList();
            if (item == null) {
                itemService.publish(userId, category.getId(), nameField.getText(),
                        description, price, tags);
            } else {
                itemService.update(item.getId(), userId, category.getId(), nameField.getText(),
                        description, price, tags);
            }
            owner.reloadItems();
        } catch (BusinessException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(owner, e.getMessage(), "保存失败",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}
