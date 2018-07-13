package priv.moltmann.locationlogger;

import org.junit.Test;
import org.mockito.MockitoSession;

import static com.android.dx.mockito.inline.extended.ExtendedMockito.mock;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.mockitoSession;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.spy;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.spyOn;
import static com.android.dx.mockito.inline.extended.ExtendedMockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UnitTests {
    class T {
        String echo(String in) {
            return in;
        }
    }

    @Test
    public void testStubbing() {
        T mockT = mock(T.class);
        // mock objects return a default value when not stubbed
        assertNull(mockT.echo("Marco"));

        when(mockT.echo("Marco")).thenReturn("Polo");
        assertEquals("Polo", mockT.echo("Marco"));
    }

    @Test
    public void testStubbingOnSpy() {
        T spyT = spy(new T());
        assertEquals("Marco", spyT.echo("Marco"));

        when(spyT.echo("Marco")).thenReturn("Polo");
        assertEquals("Polo", spyT.echo("Marco"));
    }

    class F {
        final String finalEcho(String in) {
            return in;
        }
    }

    @Test
    public void testStubbingFinalMethod() {
        F mockF = mock(F.class);
        when(mockF.finalEcho("Marco")).thenReturn("Polo");

        assertEquals("Polo", mockF.finalEcho("Marco"));
    }

    static class S {
        static String staticEcho(String in) {
            return in;
        }
    }

    @Test
    public void testStubbingStaticMethod() {
        MockitoSession session = mockitoSession().spyStatic(S.class).startMocking();
        try {
            when(S.staticEcho("Marco")).thenReturn("Polo");
            assertEquals("Polo", S.staticEcho("Marco"));
        } finally {
            session.finishMocking();
        }

        // Once the session is finished, all stubbings are reset
        assertEquals("Marco", S.staticEcho("Marco"));
    }

    @Test
    public void testSpyOn() {
        T originalT = new T();
        spyOn(originalT); // Returns void

        when(originalT.echo("Marco")).thenReturn("Polo");
        assertEquals("Polo", originalT.echo("Marco"));
    }
}
