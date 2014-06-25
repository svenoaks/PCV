#include <stdarg.h>
#include <re.h>
#include <and_log.h>

int re_alog_write(int pri, const char *tag, const char *fmt, ...)
{
	va_list ap;
	int err;
	char *strp;

	va_start(ap, fmt);
	err = re_vsdprintf(&strp, fmt, ap);
	va_end(ap);

	if (!err)
	{
		err = __android_log_write((int) pri, tag, strp);
		mem_deref(strp);
	}

	return err;
}

