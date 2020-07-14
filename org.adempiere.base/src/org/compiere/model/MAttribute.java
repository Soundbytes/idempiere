/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CCache;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 *  Product Attribute
 *
 *	@author Jorg Janke
 *	@version $Id: MAttribute.java,v 1.3 2006/07/30 00:51:03 jjanke Exp $
 */
public class MAttribute extends X_M_Attribute
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7869800574413317999L;


	/**
	 * 	Get Attributes Of Client
	 *	@param ctx Properties
	 *	@param onlyProductAttributes only Product Attributes
	 *	@param onlyListAttributes only List Attributes
	 *	@return array of attributes
	 */
	public static MAttribute[] getOfClient(Properties ctx, 
		boolean onlyProductAttributes, boolean onlyListAttributes)
	{
		int AD_Client_ID = Env.getAD_Client_ID(ctx);
		String sql = "";
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(AD_Client_ID);
		if (onlyProductAttributes)
			{
				sql += " AND IsInstanceAttribute=?";
				params.add(false);
			}
		if (onlyListAttributes)
			{
				sql += " AND AttributeValueType=?";
				params.add(MAttribute.ATTRIBUTEVALUETYPE_List);
			}
		StringBuilder whereClause = new StringBuilder("AD_Client_ID=?").append(sql);
		
		List<MAttribute>list = new Query(ctx,I_M_Attribute.Table_Name,whereClause.toString(),null)
		.setParameters(params)
		.setOnlyActiveRecords(true)
		.setOrderBy("Name")
		.list();

		MAttribute[] retValue = new MAttribute[list.size ()];
		list.toArray (retValue);
		if (s_log.isLoggable(Level.FINE)) s_log.fine("AD_Client_ID=" + AD_Client_ID + " - #" + list.size());
		return retValue;
	}	//	getOfClient
	
	/**	Logger	*/
	private static CLogger s_log = CLogger.getCLogger (MAttribute.class);

	
	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param M_Attribute_ID id
	 *	@param trxName transaction
	 */
	public MAttribute (Properties ctx, int M_Attribute_ID, String trxName)
	{
		super (ctx, M_Attribute_ID, trxName);
		if (M_Attribute_ID == 0)
		{
			setAttributeValueType(ATTRIBUTEVALUETYPE_StringMax40);
			setIsInstanceAttribute (false);
			setIsMandatory (false);
		}
	}	//	MAttribute

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName transaction
	 */
	public MAttribute (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MAttribute

	/**	Values						*/
	private MAttributeValue[]		m_values = null;

	/**
	 *	Get Values if List
	 *	@return Values or null if not list
	 */
	public MAttributeValue[] getMAttributeValues()
	{
		if (m_values == null && ATTRIBUTEVALUETYPE_List.equals(getAttributeValueType()))
		{
			final String whereClause = I_M_AttributeValue.COLUMNNAME_M_Attribute_ID+"=?";
			List<MAttributeValue> list = new ArrayList<MAttributeValue>();
			if (!isMandatory())
				list.add (null);
			list = new Query(getCtx(),I_M_AttributeValue.Table_Name,whereClause,null)
			.setParameters(getM_Attribute_ID())
			.setOrderBy("Value")
			.list();
			m_values = new MAttributeValue[list.size()];
			list.toArray(m_values);
		}
		return m_values;
	}	//	getValues

	static private CCache<Integer,MAttribute> s_cache = new CCache<Integer,MAttribute>(Table_Name, 30, 60);
	
	public static MAttribute get (Properties ctx, int M_Attribute_ID){
		Integer key = Integer.valueOf(M_Attribute_ID);
		MAttribute retValue = (MAttribute)s_cache.get(key);
		
		if(retValue!=null)
			return retValue;
		
		retValue = (MAttribute) MTable.get(ctx, MAttribute.Table_ID).getPO(M_Attribute_ID, null);
		
		s_cache.put(key, retValue);
		
		return retValue;
	}
	
	/**************************************************************************
	 * 	Get Attribute Instance
	 *	@param M_AttributeSetInstance_ID attribute set instance
	 *	@return Attribute Instance or null
	 */
	public MAttributeInstance getMAttributeInstance (int M_AttributeSetInstance_ID)
	{
		final String whereClause = I_M_AttributeInstance.COLUMNNAME_M_Attribute_ID+"=? AND "+I_M_AttributeInstance.COLUMNNAME_M_AttributeSetInstance_ID+"=?";
		MAttributeInstance retValue = new Query(getCtx(),I_M_AttributeInstance.Table_Name,whereClause,get_TrxName())
		.setParameters(getM_Attribute_ID(),M_AttributeSetInstance_ID)
		.first();

		return retValue;
	}	//	getAttributeInstance

	
	// TODO: Merge Patch-2999 lines  40-99
	/**
	 * 	Set Attribute Instance
	 * 	@param value value
	 * 	@param M_AttributeSetInstance_ID id
	 */
	public void setMAttributeInstance (int M_AttributeSetInstance_ID, MAttributeValue value)
	{
		MAttributeInstance instance = getMAttributeInstance(M_AttributeSetInstance_ID);
		if (instance == null)
		{
			if (value != null)
				instance = new MAttributeInstance (getCtx (), getM_Attribute_ID (),
					M_AttributeSetInstance_ID, value.getM_AttributeValue_ID (),
					value.getName (), get_TrxName()); 					//	Cached !!
			else
				instance = new MAttributeInstance (getCtx(), getM_Attribute_ID(),
					M_AttributeSetInstance_ID, 0, null, get_TrxName());
		}
		else
		{
			if (value != null)
			{
				instance.setM_AttributeValue_ID (value.getM_AttributeValue_ID ());
				instance.setValue (value.getName()); 	//	Cached !!
			}
			else
			{
				instance.setM_AttributeValue_ID (0);
				instance.setValue (null);
			}
		}
		instance.saveEx();
	}	//	setAttributeInstance

	/**
	 * 	Set Attribute Instance
	 * 	@param value string value
	 * 	@param M_AttributeSetInstance_ID id
	 */
	public void setMAttributeInstance (int M_AttributeSetInstance_ID, String value)
	{
		MAttributeInstance instance = getMAttributeInstance(M_AttributeSetInstance_ID);
		if (instance == null)
			instance = new MAttributeInstance (getCtx(), getM_Attribute_ID(), 
				M_AttributeSetInstance_ID, value, get_TrxName());
		else
			instance.setValue(value);
		instance.saveEx();
	}	//	setAttributeInstance

	/**
	 * 	Set Attribute Instance
	 * 	@param value number value
	 * 	@param M_AttributeSetInstance_ID id
	 */
	public void setMAttributeInstance (int M_AttributeSetInstance_ID, BigDecimal value)
	{
		MAttributeInstance instance = getMAttributeInstance(M_AttributeSetInstance_ID);
		if (instance == null)
			instance = new MAttributeInstance (getCtx(), getM_Attribute_ID(), 
				M_AttributeSetInstance_ID, value, get_TrxName());
		else
			instance.setValueNumber(value);
		instance.saveEx();
	}	//	setAttributeInstance
	
	public void setMAttributeInstance (int M_AttributeSetInstance_ID, int valueInt, String value)
	{
		MAttributeInstance instance = getMAttributeInstance(M_AttributeSetInstance_ID);
		if (instance == null)
		{
			instance = new MAttributeInstance(getCtx(), getM_Attribute_ID(),
					M_AttributeSetInstance_ID, valueInt, get_TrxName());
		}
		
		instance.setValueInt(valueInt, value);
		instance.saveEx();
	}	//	setAttributeInstance
	
	public void setMAttributeInstance(int M_AttributeSetInstance_ID, Timestamp value)
	{
		MAttributeInstance instance = getMAttributeInstance(M_AttributeSetInstance_ID);
		if (instance == null)
			instance = new MAttributeInstance(getCtx(), getM_Attribute_ID(), M_AttributeSetInstance_ID, value,
					get_TrxName());
		else
			instance.setValueDate(value);
		instance.saveEx();
	}// setAttributeInstance

	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuilder sb = new StringBuilder ("MAttribute[");
		sb.append (get_ID()).append ("-").append (getName())
			.append(",Type=").append(getAttributeValueType())
			.append(",Instance=").append(isInstanceAttribute())
			.append ("]");
		return sb.toString ();
	}	//	toString
	
	/**
	 * 	AfterSave
	 *	@param newRecord new
	 *	@param success success
	 *	@return success
	 */
	protected boolean afterSave (boolean newRecord, boolean success)
	{
		if (!success)
			return success;
		//	Changed to Instance Attribute
		if (!newRecord && is_ValueChanged("IsInstanceAttribute") && isInstanceAttribute())
		{
			StringBuilder sql = new StringBuilder("UPDATE M_AttributeSet mas ")
				.append("SET IsInstanceAttribute='Y' ")
				.append("WHERE IsInstanceAttribute='N'")
				.append(" AND EXISTS (SELECT * FROM M_AttributeUse mau ")
					.append("WHERE mas.M_AttributeSet_ID=mau.M_AttributeSet_ID")
					.append(" AND mau.M_Attribute_ID=").append(getM_Attribute_ID()).append(")");
			int no = DB.executeUpdate(sql.toString(), get_TrxName());
			if (log.isLoggable(Level.FINE)) log.fine("AttributeSet Instance set #" + no);
		}
		return success;
	}	//	afterSave
	
}	//	MAttribute
