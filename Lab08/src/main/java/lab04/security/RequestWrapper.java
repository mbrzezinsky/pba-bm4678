package lab04.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.util.ContentCachingRequestWrapper;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

public class RequestWrapper extends ContentCachingRequestWrapper
{
    private final byte[] buffer;
    public RequestWrapper(HttpServletRequest request)
    {
        super(request);
        buffer = new byte[request.getContentLength()];
        try
        {
            request.getInputStream().read(buffer);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        return new CachedServletInputStream(new ByteArrayInputStream(buffer));
    }

    private class CachedServletInputStream extends ServletInputStream
    {
        private final InputStream inputStream;
        public CachedServletInputStream(InputStream inputStream)
        {
            this.inputStream = inputStream;
        }
        @Override
        public boolean isFinished()
        {
            try
            {
                return inputStream.available() == 0;
            }
            catch (IOException e)
            {
                return true;
            }
        }
        @Override
        public boolean isReady()
        {
            return true;
        }
        @Override
        public void setReadListener(ReadListener readListener)
        {
            throw new UnsupportedOperationException();
        }
        @Override
        public int read() throws IOException
        {
            return inputStream.read();
        }
    }
}
