package com.studyplanner.ui;

import com.studyplanner.app.AppConfig;
import com.studyplanner.app.Application;
import com.studyplanner.ui.analytics.AnalyticsPanel;
import com.studyplanner.ui.calendar.CalendarPanel;
import com.studyplanner.ui.components.HeaderPanel;
import com.studyplanner.ui.components.SidebarPanel;
import com.studyplanner.ui.dashboard.DashboardPanel;
import com.studyplanner.ui.focus.FocusPanel;
import com.studyplanner.ui.projects.ProjectsPanel;
import com.studyplanner.ui.settings.SettingsPanel;
import com.studyplanner.ui.subjects.SubjectsPanel;
import com.studyplanner.ui.tasks.TaskDialog;
import com.studyplanner.ui.tasks.TasksPanel;
import com.studyplanner.util.ColorPalette;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main application window for StudyPlanner Pro.
 * Houses the left sidebar, top header, CardLayout content pages, and data synchronization.
 */
public class MainFrame extends Frame implements SidebarPanel.NavigationListener {
    private final CardLayout cardLayout;
    private final Panel contentPanel;
    private final HeaderPanel headerPanel;
    private final SidebarPanel sidebarPanel;

    // View panels
    private final DashboardPanel dashboardPanel;
    private final TasksPanel tasksPanel;
    private final ProjectsPanel projectsPanel;
    private final SubjectsPanel subjectsPanel;
    private final CalendarPanel calendarPanel;
    private final FocusPanel focusPanel;
    private final AnalyticsPanel analyticsPanel;
    private final SettingsPanel settingsPanel;

    public MainFrame() {
        super(AppConfig.APP_NAME + " - " + AppConfig.APP_TAGLINE);
        setLayout(new BorderLayout());
        setBackground(ColorPalette.BG);
        setSize(AppConfig.DEFAULT_WIDTH, AppConfig.DEFAULT_HEIGHT);
        setMinimumSize(new Dimension(AppConfig.MIN_WIDTH, AppConfig.MIN_HEIGHT));
        setLocationRelativeTo(null); // Center on screen

        // Left Navigation Sidebar
        sidebarPanel = new SidebarPanel(this);
        add(sidebarPanel, BorderLayout.WEST);

        // Center Content Area (Header on NORTH, CardLayout in CENTER)
        Panel rightArea = new Panel(new BorderLayout());
        rightArea.setBackground(ColorPalette.BG);

        headerPanel = new HeaderPanel("Dashboard", e -> openNewTaskDialog());
        rightArea.add(headerPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new Panel(cardLayout);
        contentPanel.setBackground(ColorPalette.BG);

        // Instantiate view panels
        dashboardPanel = new DashboardPanel(this);
        tasksPanel = new TasksPanel(this);
        projectsPanel = new ProjectsPanel(this);
        subjectsPanel = new SubjectsPanel(this);
        calendarPanel = new CalendarPanel(this);
        focusPanel = new FocusPanel();
        analyticsPanel = new AnalyticsPanel();
        settingsPanel = new SettingsPanel(this);

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(tasksPanel, "tasks");
        contentPanel.add(projectsPanel, "projects");
        contentPanel.add(subjectsPanel, "subjects");
        contentPanel.add(calendarPanel, "calendar");
        contentPanel.add(focusPanel, "focus");
        contentPanel.add(analyticsPanel, "analytics");
        contentPanel.add(settingsPanel, "settings");

        rightArea.add(contentPanel, BorderLayout.CENTER);
        add(rightArea, BorderLayout.CENTER);

        // Register application-wide change listener to refresh views automatically
        Application.getInstance().addChangeListener(this::refreshActiveViews);

        // Safe window close handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                focusPanel.cleanup();
                dispose();
                System.exit(0);
            }
        });
    }

    private void openNewTaskDialog() {
        TaskDialog dlg = new TaskDialog(this, null);
        dlg.setVisible(true);
    }

    private String currentPageKey = "dashboard";

    @Override
    public void onNavigate(String pageKey) {
        this.currentPageKey = pageKey;
        cardLayout.show(contentPanel, pageKey);

        String title = "Dashboard";
        switch (pageKey) {
            case "tasks": title = "Task Management"; break;
            case "projects": title = "Project Milestones"; break;
            case "subjects": title = "Course & Subject Catalog"; break;
            case "calendar": title = "Monthly Calendar & Deadlines"; break;
            case "focus": title = "Study Focus & Pomodoro Timer"; break;
            case "analytics": title = "Productivity Analytics & Reports"; break;
            case "settings": title = "Application Settings"; break;
        }

        headerPanel.setPageTitle(title);
        headerPanel.refreshDate();
        refreshCurrentView();
    }

    private void refreshCurrentView() {
        switch (currentPageKey) {
            case "dashboard": dashboardPanel.refreshDashboard(); break;
            case "tasks": tasksPanel.refreshTasks(); break;
            case "projects": projectsPanel.refreshProjects(); break;
            case "subjects": subjectsPanel.refreshSubjects(); break;
            case "calendar": calendarPanel.refreshCalendar(); break;
            case "focus": focusPanel.refreshSelectors(); break;
            case "analytics": analyticsPanel.refreshAnalytics(); break;
        }
    }

    private void refreshActiveViews() {
        refreshCurrentView();
    }
}
