package com.campus.recycle.ui;

import com.campus.recycle.exception.BusinessException;
import com.campus.recycle.model.Category;
import com.campus.recycle.model.Item;
import com.campus.recycle.model.ItemSearchCriteria;
import com.campus.recycle.model.ItemStatus;
import com.campus.recycle.model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/** Main item-browsing and listing-management window. */
final class MainFrame extends JFrame {

    private final AppServices services;
    private final User user;
    private final Runnable onLogout;
    private final ItemTableModel tableModel = new ItemTableModel();
    private final JTable itemTable = new JTable(tableModel);
    private final JComboBox<CategoryChoice> categoryFilter = new JComboBox<>();
    private final JComboBox<StatusChoice> statusFilter = new JComboBox<>();
    private final JTextField keywordField = new JTextField(18);
    private final JTextField minPriceField = new JTextField(7);
    private final JTextField maxPriceField = new JTextField(7);
    private final List<Category> categories;

    MainFrame(AppServices services, User user, Runnable onLogout) {
        super("低碳校园资源循环流转系统");
        this.services = services;
        this.user = user;
        this.onLogout = onLogout;
        categories = services.getCategoryService().findAll();

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        header.add(new JLabel("校园闲置物品"), BorderLayout.WEST);
        String displayName = user.getNickname() == null || user.getNickname().isBlank()
                ? user.getUsername() : user.getNickname();
        JPanel account = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        account.add(new JLabel(displayName + "（" + user.getRole().getDisplayName() + "）"));
        JButton logoutButton = new JButton("退出登录");
        logoutButton.addActionListener(event -> logout());
        account.add(logoutButton);
        header.add(account, BorderLayout.EAST);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("关键词"));
        filters.add(keywordField);
        filters.add(new JLabel("分类"));
        categoryFilter.addItem(new CategoryChoice(null, "全部分类"));
        categories.forEach(category ->
                categoryFilter.addItem(new CategoryChoice(category.getId(), category.getName())));
        filters.add(categoryFilter);
        filters.add(new JLabel("状态"));
        statusFilter.addItem(new StatusChoice(null, "全部状态"));
        for (ItemStatus status : ItemStatus.values()) {
            statusFilter.addItem(new StatusChoice(status, status.getDisplayName()));
        }
        statusFilter.setSelectedItem(new StatusChoice(ItemStatus.AVAILABLE,
                ItemStatus.AVAILABLE.getDisplayName()));
        filters.add(statusFilter);
        filters.add(new JLabel("价格"));
        filters.add(minPriceField);
        filters.add(new JLabel("至"));
        filters.add(maxPriceField);
        JButton searchButton = new JButton("搜索");
        searchButton.addActionListener(event -> searchItems());
        keywordField.addActionListener(event -> searchItems());
        filters.add(searchButton);

        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setAutoCreateRowSorter(true);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton publishButton = new JButton("发布物品");
        JButton editButton = new JButton("编辑我的物品");
        JButton removeButton = new JButton("下架我的物品");
        JButton mineButton = new JButton("我的发布");
        JButton allButton = new JButton("浏览全部");
        publishButton.addActionListener(event -> openItemDialog(null));
        editButton.addActionListener(event -> editSelectedItem());
        removeButton.addActionListener(event -> removeSelectedItem());
        mineButton.addActionListener(event -> showMyItems());
        allButton.addActionListener(event -> searchItems());
        actions.add(publishButton);
        actions.add(editButton);
        actions.add(removeButton);
        actions.add(mineButton);
        actions.add(allButton);

        JPanel top = new JPanel(new BorderLayout());
        top.add(header, BorderLayout.NORTH);
        top.add(filters, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(itemTable), BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 560);
        setLocationRelativeTo(null);
        searchItems();
    }

    void reloadItems() {
        searchItems();
    }

    private void searchItems() {
        try {
            ItemSearchCriteria.Builder builder = ItemSearchCriteria.builder()
                    .keyword(keywordField.getText().trim());
            CategoryChoice category = (CategoryChoice) categoryFilter.getSelectedItem();
            StatusChoice status = (StatusChoice) statusFilter.getSelectedItem();
            if (category != null && category.id() != null) {
                builder.categoryId(category.id());
            }
            if (status != null && status.status() != null) {
                builder.status(status.status());
            }
            String min = minPriceField.getText().trim();
            String max = maxPriceField.getText().trim();
            builder.priceBetween(parseOptionalPrice(min, "最低价"),
                    parseOptionalPrice(max, "最高价"));
            tableModel.setItems(services.getItemService().search(builder.build()), categories);
        } catch (BusinessException | IllegalArgumentException e) {
            showError(e);
        }
    }

    private Double parseOptionalPrice(String value, String label) {
        if (value.isEmpty()) {
            return null;
        }
        try {
            double price = Double.parseDouble(value);
            if (!Double.isFinite(price) || price < 0) {
                throw new IllegalArgumentException(label + "必须是有限的非负数");
            }
            return price;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + "必须是有效数字");
        }
    }

    private void showMyItems() {
        try {
            tableModel.setItems(services.getItemService().search(
                    ItemSearchCriteria.builder().sellerId(user.getId()).build()), categories);
        } catch (BusinessException e) {
            showError(e);
        }
    }

    private void openItemDialog(Item item) {
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有可用分类，暂时无法发布物品",
                    "操作失败", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new ItemDialog(this, services.getItemService(), user.getId(), categories, item);
    }

    private void editSelectedItem() {
        Item item = selectedItem();
        if (item == null) {
            return;
        }
        if (!item.getSellerId().equals(user.getId())) {
            JOptionPane.showMessageDialog(this, "只能编辑自己发布的物品",
                    "无权操作", JOptionPane.WARNING_MESSAGE);
            return;
        }
        openItemDialog(item);
    }

    private void removeSelectedItem() {
        Item item = selectedItem();
        if (item == null) {
            return;
        }
        if (!item.getSellerId().equals(user.getId())) {
            JOptionPane.showMessageDialog(this, "只能下架自己发布的物品",
                    "无权操作", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "确定下架「" + item.getName() + "」吗？",
                "确认下架", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            services.getItemService().remove(item.getId(), user.getId());
            searchItems();
        } catch (BusinessException e) {
            showError(e);
        }
    }

    private Item selectedItem() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条物品记录",
                    "提示", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return tableModel.getItemAt(itemTable.convertRowIndexToModel(selectedRow));
    }

    private void logout() {
        dispose();
        onLogout.run();
    }

    private void showError(RuntimeException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "操作失败",
                JOptionPane.WARNING_MESSAGE);
    }

    private record CategoryChoice(Integer id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private record StatusChoice(ItemStatus status, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
