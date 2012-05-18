/*
 * Copyright (c) 2008, 2009
 *  Rough Diamond Co., Ltd.              -- http://www.rough-diamond.co.jp/
 *  Information Systems Institute, Ltd.  -- http://www.isken.co.jp/
 *  All rights reserved.
 */
package jp.rough_diamond.framework.service.db;

import java.sql.SQLException;

public class VersionUnmatchException extends SQLException {
	public final static long serialVersionUID = -1L;

	public VersionUnmatchException() {
		super();
	}

	public VersionUnmatchException(String reason, String sqlState, int vendorCode, Throwable cause) {
		super(reason, sqlState, vendorCode, cause);
	}

	public VersionUnmatchException(String reason, String SQLState, int vendorCode) {
		super(reason, SQLState, vendorCode);
	}

	public VersionUnmatchException(String reason, String sqlState, Throwable cause) {
		super(reason, sqlState, cause);
	}

	public VersionUnmatchException(String reason, String SQLState) {
		super(reason, SQLState);
	}

	public VersionUnmatchException(String reason, Throwable cause) {
		super(reason, cause);
	}

	public VersionUnmatchException(String reason) {
		super(reason);
	}

	public VersionUnmatchException(Throwable cause) {
		super(cause);
	}
}
