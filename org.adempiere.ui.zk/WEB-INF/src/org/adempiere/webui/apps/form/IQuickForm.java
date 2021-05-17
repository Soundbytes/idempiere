/******************************************************************************
 * Copyright (C) 2016 Logilite Technologies LLP								  *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/

package org.adempiere.webui.apps.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adempiere.util.Callback;
import org.adempiere.webui.adwindow.AbstractADWindowContent;
import org.adempiere.webui.adwindow.QuickGridView;
import org.adempiere.webui.component.Borderlayout;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.component.ZkCssHelper;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.adempiere.webui.window.CustomizeGridViewDialog;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.DataStatusEvent;
import org.compiere.model.DataStatusListener;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MRole;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
import org.zkforge.keylistener.Keylistener;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;

/**
 * Quick entry form
 * 
 * @author Logilite Technologies
 * @since Nov 03, 2017
 */
public interface IQuickForm extends EventListener <Event>, DataStatusListener
{
//	public Trx trx = null;

//	public IQuickForm(AbstractADWindowContent winContent, boolean m_onlyCurrentRows, int m_onlyCurrentDays);

	public void onCancel();

	public void onUnSort();

	public void onCustomize();

	public void onIgnore();

	public void onDelete();

	public void onSave();

	public void onRefresh();

	public void dispose();
}