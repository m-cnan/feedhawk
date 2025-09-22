import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

public class FeedHawkMainWindow extends JFrame {
    private JPanel mainPanel;
    private JPanel feedPanel;
    private JScrollPane feedScrollPane;
    private JPanel sidePanel;
    private JList<String> listsList;
    private DefaultListModel<String> listsModel;
    private JButton refreshButton;
    private JButton bookmarksButton;
    private JButton settingsButton;
    private JLabel statusLabel;
    
    // Mock data for demonstration
    private List<FeedItem> currentFeeds;
    private String currentUser = "demo_user";
    
    public FeedHawkMainWindow() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadMockData();
        
        setTitle("FeedHawk - RSS Feed Reader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout());
        
        // Side panel for lists and controls
        sidePanel = new JPanel(new BorderLayout());
        sidePanel.setPreferredSize(new Dimension(250, 0));
        sidePanel.setBackground(new Color(248, 249, 250));
        sidePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Lists
        listsModel = new DefaultListModel<>();
        listsModel.addElement("🏠 Home");
        listsModel.addElement("🔖 Bookmarks");
        listsModel.addElement("⚙️ Tech");
        listsModel.addElement("🏃‍♂️ Sports");
        listsModel.addElement("📰 News");
        
        listsList = new JList<>(listsModel);
        listsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listsList.setSelectedIndex(0);
        listsList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        listsList.setCellRenderer(new ListCellRenderer<String>() {
            private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
            
            @Override
            public Component getListCellRendererComponent(JList<? extends String> list, String value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) defaultRenderer.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(8, 12, 8, 12));
                
                if (isSelected) {
                    label.setBackground(new Color(0, 123, 255));
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(Color.BLACK);
                }
                
                return label;
            }
        });
        
        // Control buttons
        refreshButton = new JButton("🔄 Refresh");
        bookmarksButton = new JButton("🔖 View Bookmarks");
        settingsButton = new JButton("⚙️ Settings");
        
        styleButton(refreshButton);
        styleButton(bookmarksButton);
        styleButton(settingsButton);
        
        // Feed panel (main content area)
        feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBackground(Color.WHITE);
        
        feedScrollPane = new JScrollPane(feedPanel);
        feedScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        feedScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        feedScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Status bar
        statusLabel = new JLabel("Ready - " + currentUser);
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(Color.GRAY);
        
        currentFeeds = new ArrayList<>();
    }
    
    private void styleButton(JButton button) {
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setBackground(new Color(248, 249, 250));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void setupLayout() {
        // Side panel layout
        JPanel listsPanel = new JPanel(new BorderLayout());
        listsPanel.add(new JLabel("📋 Your Lists"), BorderLayout.NORTH);
        listsPanel.add(new JScrollPane(listsList), BorderLayout.CENTER);
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.add(Box.createVerticalStrut(10));
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(Box.createVerticalStrut(5));
        buttonsPanel.add(bookmarksButton);
        buttonsPanel.add(Box.createVerticalStrut(5));
        buttonsPanel.add(settingsButton);
        buttonsPanel.setOpaque(false);
        
        sidePanel.add(listsPanel, BorderLayout.CENTER);
        sidePanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Main panel layout
        mainPanel.add(sidePanel, BorderLayout.WEST);
        mainPanel.add(feedScrollPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void setupEventHandlers() {
        listsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedList = listsList.getSelectedValue();
                loadFeedsForList(selectedList);
                statusLabel.setText("Loaded " + selectedList + " - " + currentFeeds.size() + " items");
            }
        });
        
        refreshButton.addActionListener(e -> {
            statusLabel.setText("Refreshing feeds...");
            // Simulate refresh delay
            Timer timer = new Timer(1000, evt -> {
                loadMockData();
                statusLabel.setText("Feeds refreshed - " + currentFeeds.size() + " items");
            });
            timer.setRepeats(false);
            timer.start();
        });
        
        bookmarksButton.addActionListener(e -> {
            listsList.setSelectedIndex(1); // Select bookmarks
        });
        
        settingsButton.addActionListener(e -> {
            showSettingsDialog();
        });
    }
    
    private void loadFeedsForList(String listName) {
        feedPanel.removeAll();
        
        // Filter feeds based on selected list
        List<FeedItem> filteredFeeds = new ArrayList<>(currentFeeds);
        
        if (listName.contains("Bookmarks")) {
            filteredFeeds = currentFeeds.stream()
                .filter(FeedItem::isBookmarked)
                .toList();
        } else if (listName.contains("Tech")) {
            filteredFeeds = currentFeeds.stream()
                .filter(f -> f.getCategory().equals("Tech"))
                .toList();
        } else if (listName.contains("Sports")) {
            filteredFeeds = currentFeeds.stream()
                .filter(f -> f.getCategory().equals("Sports"))
                .toList();
        } else if (listName.contains("News")) {
            filteredFeeds = currentFeeds.stream()
                .filter(f -> f.getCategory().equals("News"))
                .toList();
        }
        
        for (FeedItem feed : filteredFeeds) {
            feedPanel.add(createFeedCard(feed));
            feedPanel.add(Box.createVerticalStrut(10));
        }
        
        feedPanel.revalidate();
        feedPanel.repaint();
        
        // Scroll to top
        SwingUtilities.invokeLater(() -> {
            feedScrollPane.getViewport().setViewPosition(new Point(0, 0));
        });
    }
    
    private JPanel createFeedCard(FeedItem feed) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(228, 230, 235), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        // Header with source and date
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel sourceLabel = new JLabel("📡 " + feed.getSourceName());
        sourceLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        sourceLabel.setForeground(new Color(0, 123, 255));
        
        JLabel dateLabel = new JLabel(feed.getFormattedDate());
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        dateLabel.setForeground(Color.GRAY);
        
        header.add(sourceLabel, BorderLayout.WEST);
        header.add(dateLabel, BorderLayout.EAST);
        
        // Title
        JLabel titleLabel = new JLabel("<html><h3 style='margin: 5px 0;'>" + feed.getTitle() + "</h3></html>");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        // Content preview
        JLabel contentLabel = new JLabel("<html><p style='margin: 0; color: #666;'>" + 
            feed.getSummary() + "</p></html>");
        contentLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        actionPanel.setOpaque(false);
        
        JButton readButton = new JButton(feed.isRead() ? "✅ Read" : "📖 Mark Read");
        JButton bookmarkButton = new JButton(feed.isBookmarked() ? "🔖 Bookmarked" : "🔖 Bookmark");
        JButton openButton = new JButton("🔗 Open");
        
        styleActionButton(readButton);
        styleActionButton(bookmarkButton);
        styleActionButton(openButton);
        
        // Button actions
        readButton.addActionListener(e -> {
            feed.setRead(!feed.isRead());
            readButton.setText(feed.isRead() ? "✅ Read" : "📖 Mark Read");
        });
        
        bookmarkButton.addActionListener(e -> {
            feed.setBookmarked(!feed.isBookmarked());
            bookmarkButton.setText(feed.isBookmarked() ? "🔖 Bookmarked" : "🔖 Bookmark");
        });
        
        openButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(java.net.URI.create(feed.getUrl()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not open URL: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        actionPanel.add(readButton);
        actionPanel.add(Box.createHorizontalStrut(5));
        actionPanel.add(bookmarkButton);
        actionPanel.add(Box.createHorizontalStrut(5));
        actionPanel.add(openButton);
        
        // Main content panel
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(contentLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(actionPanel);
        
        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    private void styleActionButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.PLAIN, 11));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        button.setBackground(new Color(248, 249, 250));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(233, 236, 239));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(248, 249, 250));
            }
        });
    }
    
    private void loadMockData() {
        currentFeeds.clear();
        
        // Add some mock feed items
        currentFeeds.add(new FeedItem(
            "OpenAI Announces GPT-5 with Revolutionary Capabilities",
            "OpenAI has unveiled GPT-5, featuring unprecedented reasoning abilities and multimodal understanding that surpasses all previous models...",
            "TechCrunch",
            "Tech",
            "https://techcrunch.com/gpt5-announcement",
            "2025-09-11 10:30",
            false,
            false
        ));
        
        currentFeeds.add(new FeedItem(
            "Tesla's New Battery Technology Promises 1000-Mile Range",
            "Tesla's latest battery breakthrough could revolutionize electric vehicles with unprecedented range and faster charging times...",
            "Ars Technica",
            "Tech",
            "https://arstechnica.com/tesla-battery-breakthrough",
            "2025-09-11 09:15",
            true,
            true
        ));
        
        currentFeeds.add(new FeedItem(
            "Championship Final: Thrilling Match Ends in Overtime Victory",
            "In a heart-stopping finale, the underdog team secured victory in a dramatic overtime finish that had fans on the edge of their seats...",
            "ESPN",
            "Sports",
            "https://espn.com/championship-final-overtime",
            "2025-09-10 22:45",
            false,
            false
        ));
        
        currentFeeds.add(new FeedItem(
            "Global Climate Summit Reaches Historic Agreement",
            "World leaders have reached a groundbreaking consensus on climate action, setting ambitious targets for the next decade...",
            "BBC News",
            "News",
            "https://bbc.co.uk/climate-summit-agreement",
            "2025-09-10 16:20",
            false,
            true
        ));
        
        currentFeeds.add(new FeedItem(
            "Quantum Computing Breakthrough Achieved by Research Team",
            "Scientists have demonstrated a quantum computer that maintains coherence for record-breaking duration, opening new possibilities for practical applications...",
            "Scientific American",
            "Tech",
            "https://scientificamerican.com/quantum-breakthrough",
            "2025-09-10 14:10",
            true,
            false
        ));
        
        // Load feeds for currently selected list
        String selectedList = listsList.getSelectedValue();
        if (selectedList != null) {
            loadFeedsForList(selectedList);
        }
    }
    
    private void showSettingsDialog() {
        JDialog settingsDialog = new JDialog(this, "Settings", true);
        settingsDialog.setSize(400, 300);
        settingsDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("⚙️ FeedHawk Settings");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        
        settingsPanel.add(new JLabel("🔄 Auto-refresh interval:"));
        JComboBox<String> refreshCombo = new JComboBox<>(new String[]{"5 minutes", "15 minutes", "30 minutes", "1 hour"});
        refreshCombo.setMaximumSize(new Dimension(200, 25));
        refreshCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsPanel.add(refreshCombo);
        
        settingsPanel.add(Box.createVerticalStrut(15));
        
        settingsPanel.add(new JLabel("📱 Notifications:"));
        JCheckBox notificationsCheck = new JCheckBox("Enable desktop notifications");
        notificationsCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsPanel.add(notificationsCheck);
        
        settingsPanel.add(Box.createVerticalStrut(15));
        
        settingsPanel.add(new JLabel("🎨 Theme:"));
        JComboBox<String> themeCombo = new JComboBox<>(new String[]{"Light", "Dark", "Auto"});
        themeCombo.setMaximumSize(new Dimension(200, 25));
        themeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsPanel.add(themeCombo);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("💾 Save");
        JButton cancelButton = new JButton("❌ Cancel");
        
        saveButton.addActionListener(e -> {
            settingsDialog.dispose();
            statusLabel.setText("Settings saved successfully");
        });
        
        cancelButton.addActionListener(e -> settingsDialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(settingsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        settingsDialog.add(panel);
        settingsDialog.setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new FeedHawkMainWindow().setVisible(true);
        });
    }
}

// Supporting class for feed items
class FeedItem {
    private String title;
    private String summary;
    private String sourceName;
    private String category;
    private String url;
    private String publishedAt;
    private boolean isRead;
    private boolean isBookmarked;
    
    public FeedItem(String title, String summary, String sourceName, String category, 
                   String url, String publishedAt, boolean isRead, boolean isBookmarked) {
        this.title = title;
        this.summary = summary;
        this.sourceName = sourceName;
        this.category = category;
        this.url = url;
        this.publishedAt = publishedAt;
        this.isRead = isRead;
        this.isBookmarked = isBookmarked;
    }
    
    // Getters and setters
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getSourceName() { return sourceName; }
    public String getCategory() { return category; }
    public String getUrl() { return url; }
    public String getPublishedAt() { return publishedAt; }
    public boolean isRead() { return isRead; }
    public boolean isBookmarked() { return isBookmarked; }
    
    public void setRead(boolean read) { this.isRead = read; }
    public void setBookmarked(boolean bookmarked) { this.isBookmarked = bookmarked; }
    
    public String getFormattedDate() {
        // Simple formatting for demo
        return publishedAt.substring(0, 10);
    }
}