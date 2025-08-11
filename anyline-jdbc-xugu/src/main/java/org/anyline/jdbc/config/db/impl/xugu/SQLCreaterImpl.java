 
package org.anyline.jdbc.config.db.impl.xugu;
 
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.db.OrderStore;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.impl.BasicSQLCreaterImpl;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.config.db.run.impl.TableRunSQLImpl;
import org.anyline.jdbc.exception.SQLUpdateException;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository("anyline.jdbc.creater.xugu")
public class SQLCreaterImpl extends BasicSQLCreaterImpl implements SQLCreater{ 
 
	public DB_TYPE type(){ 
		return DB_TYPE.XUGU;
	} 
	public SQLCreaterImpl(){ 
		disKeyFr = "`"; 
		disKeyTo = "`"; 
	} 
	@Override 
	public String parseFinalQueryTxt(RunSQL run){ 
		String sql = run.getBaseQueryTxt(); 
		String cols = run.getFetchColumns(); 
		if(!"*".equals(cols)){ 
			String reg = "(?i)^select[\\s\\S]+from"; 
			sql = sql.replaceAll(reg,"SELECT "+cols+" FROM "); 
		} 
		OrderStore orders = run.getOrderStore(); 
		if(null != orders){ 
			sql += orders.getRunText(getDisKeyFr()+getDisKeyTo()); 
		} 
		PageNavi navi = run.getPageNavi(); 
		if(null != navi){ 
			int limit = navi.getLastRow() - navi.getFirstRow() + 1; 
			if(limit < 0){ 
				limit = 0; 
			} 
			sql += " LIMIT " + navi.getFirstRow() + "," + limit; 
		} 
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE"); 
		return sql; 
	} 
 
	public String concat(String ... args){ 
		String result = ""; 
		if(null != args && args.length > 0){ 
			result = "concat("; 
			int size = args.length; 
			for(int i=0; i<size; i++){ 
				String arg = args[i]; 
				if(i>0){ 
					result += ","; 
				} 
				result += arg; 
			} 
			result += ")"; 
		} 
		return result; 
	}

	@Override
	public String getDisKeyFr(){
		return disKeyFr;
	}
	@Override
	public String getDisKeyTo(){
		return disKeyTo;
	}

	@Override
	public RunSQL createDeleteRunSQL(String table, String key, Object values){
		if(null == table || null == key || null == values){
			return null;
		}
		StringBuilder builder = new StringBuilder();
		TableRunSQLImpl run = new TableRunSQLImpl();
		builder.append("DELETE FROM ").append(table).append(" WHERE ");

		if(values instanceof Collection){
			Collection cons = (Collection)values;
			builder.append(getDisKeyFr()).append(key).append(getDisKeyTo());
			if(cons.size() > 1){
				builder.append(" IN(");
				int idx = 0;
				for(Object obj:cons){
					if(idx > 0){
						builder.append(",");
					}
					builder.append("?");
					idx ++;
					run.addValue(obj);
				}
				builder.append(")");
			}else if(cons.size() == 1){
				for(Object obj:cons){
					builder.append("=?");
					run.addValue(obj);
				}
			}else{
				throw new SQLUpdateException("删除异常:删除条件为空,delete方法不支持删除整表操作.");
			}
		}else{
			builder.append(getDisKeyFr()).append(key).append(getDisKeyTo());
			builder.append("=?");
			run.addValue(values);
		}
		run.setBuilder(builder);
		return run;
	}
} 
