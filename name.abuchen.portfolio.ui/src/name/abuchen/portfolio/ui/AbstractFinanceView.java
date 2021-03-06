package name.abuchen.portfolio.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.ui.util.Colors;

public abstract class AbstractFinanceView
{
    private PortfolioPart part;
    private IEclipseContext context;

    private Composite top;
    private Label title;
    private LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
    private List<Menu> contextMenus = new ArrayList<>();

    protected abstract String getDefaultTitle();
    
    protected String getTitle()
    {
        return title.getText();
    }

    protected final void updateTitle(String title)
    {
        this.title.setText(title);
    }

    /** called when some other view modifies the model */
    public void notifyModelUpdated()
    {}

    public void init(PortfolioPart part, Object parameter)
    {
        this.part = part;
    }

    public PortfolioPart getPart()
    {
        return part;
    }

    public IPreferenceStore getPreferenceStore()
    {
        return part.getPreferenceStore();
    }

    /* package */void setContext(IEclipseContext context)
    {
        this.context = context;
    }

    /* package */IEclipseContext getContext()
    {
        return this.context;
    }

    public Client getClient()
    {
        return part.getClient();
    }

    public void markDirty()
    {
        part.markDirty();
    }

    public Shell getActiveShell()
    {
        return Display.getDefault().getActiveShell();
    }

    public final void createViewControl(Composite parent)
    {
        top = new Composite(parent, SWT.NONE);
        // on windows, add a spacing line as tables
        // have white top and need a border
        int spacing = Platform.OS_WIN32.equals(Platform.getOS()) ? 1 : 0;
        GridLayoutFactory.fillDefaults().spacing(spacing, spacing).applyTo(top);

        Control header = createHeader(top);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(header);

        Control body = createBody(top);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(body);

        top.addDisposeListener(e -> dispose());
    }

    protected abstract Control createBody(Composite parent);

    private Control createHeader(Composite parent)
    {
        Composite header = new Composite(parent, SWT.NONE);
        header.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

        Font boldFont = resourceManager.createFont(FontDescriptor
                        .createFrom(JFaceResources.getFont(JFaceResources.HEADER_FONT)).setStyle(SWT.BOLD));

        title = new Label(header, SWT.NONE);
        title.setText(getDefaultTitle());
        title.setFont(boldFont);
        title.setForeground(resourceManager.createColor(Colors.HEADINGS.swt()));
        title.setBackground(header.getBackground());

        ToolBar toolBar = new ToolBar(header, SWT.FLAT | SWT.RIGHT);
        toolBar.setBackground(header.getBackground());
        addButtons(toolBar);

        // layout
        GridLayoutFactory.fillDefaults().numColumns(2).margins(5, 5).applyTo(header);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(title);
        GridDataFactory.fillDefaults().applyTo(toolBar);

        return header;
    }

    protected void addButtons(ToolBar toolBar)
    {}

    protected final void hookContextMenu(Control control, IMenuListener listener)
    {
        doCreateContextMenu(control, true, listener);
    }

    protected final Menu createContextMenu(Control control, IMenuListener listener)
    {
        return doCreateContextMenu(control, false, listener);
    }

    private final Menu doCreateContextMenu(Control control, boolean hook, IMenuListener listener)
    {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(listener);

        Menu contextMenu = menuMgr.createContextMenu(control);
        if (hook)
            control.setMenu(contextMenu);

        contextMenus.add(contextMenu);

        return contextMenu;
    }

    public void dispose()
    {
        for (Menu contextMenu : contextMenus)
            if (!contextMenu.isDisposed())
                contextMenu.dispose();

        resourceManager.dispose();
    }

    public final Control getControl()
    {
        return top;
    }

    public void setFocus()
    {
        getControl().setFocus();
    }

    public <T> T make(Class<T> type, Object... parameters)
    {
        if (parameters == null || parameters.length == 0)
            return ContextInjectionFactory.make(type, this.context);

        IEclipseContext c2 = EclipseContextFactory.create();
        for (Object param : parameters)
            c2.set(param.getClass().getName(), param);
        return ContextInjectionFactory.make(type, this.context, c2);
    }
}
