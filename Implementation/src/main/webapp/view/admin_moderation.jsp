<%@ page import="subsystems.access_profile.model.Role" %>
<%@ page import="subsystems.access_profile.model.User" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>FantaUnisa - Moderazione</title>

  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700;800&display=swap" rel="stylesheet">

  <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/admin_moderation.css">
</head>
<body>

<%
  User user = (User) session.getAttribute("user"); // Recupera l'UTENTE
  if (user == null || user.getRole() != Role.GESTORE_UTENTI) { // Controlla il RUOLO dell'utente
    response.sendRedirect(request.getContextPath() + "/view/login.jsp");
    return;
  }
%>

<jsp:include page="../includes/navbar.jsp" />

<div class="container">

  <div class="admin-tabs">
    <a class="admin-tab-btn active" href="${pageContext.request.contextPath}/ReportServlet">
      <i class="fas fa-shield-alt"></i> MODERAZIONE
    </a>
    <a class="admin-tab-btn" href="admin_upload.jsp">
      <i class="fas fa-database"></i> AGGIORNA DATI
    </a>
  </div>

  <div class="stats-header">
    <div class="stat-box">
      <span class="stat-number">${reports != null ? reports.size() : 0}</span>
      <span class="stat-label">Segnalazioni Pendenti</span>
    </div>
    <div class="stat-info">
      Gestisci con cura le segnalazioni della community per mantenere un ambiente sano.
    </div>
  </div>

  <div class="reports-container">

    <c:forEach items="${reports}" var="report">

      <div class="report-card" id="card-${report.id}">

        <div class="report-header" onclick="toggleReport(${report.id})">
          <div class="report-icon-box">
            <i class="fas fa-exclamation-triangle"></i>
          </div>

          <div class="report-main-info">
            <div class="report-title">
              SEGNALAZIONE UTENTE
              <span class="badge-post">Post #${report.postId}</span>
            </div>
            <div class="report-meta">
              Segnalato da: <strong>${report.userEmail}</strong> • ${report.dataOra}
            </div>
          </div>

          <div class="report-expand-icon">
            <i class="fas fa-chevron-down"></i>
          </div>
        </div>

        <div class="report-details" id="details-${report.id}">

          <div class="detail-row">
            <div class="detail-label">Motivo della segnalazione:</div>
            <div class="detail-content warning-text">
              "${report.motivo}"
            </div>
          </div>

          <div class="admin-actions">

            <form action="${pageContext.request.contextPath}/DeleteContentServlet" method="post">
              <input type="hidden" name="type" value="post">

              <input type="hidden" name="id" value="${report.postId}">

              <input type="hidden" name="from" value="admin">

              <button type="submit" class="btn btn-delete">
                <i class="fas fa-trash"></i> Elimina Post
              </button>
            </form>

            <form action="${pageContext.request.contextPath}/DeleteUserServlet" method="post" onsubmit="return confirm('Sei sicuro di voler bannare questo utente? Questa azione è irreversibile.');">

              <input type="hidden" name="postId" value="${report.postId}">

              <input type="hidden" name="from" value="admin">

              <button type="submit" class="btn btn-ban">
                <i class="fas fa-user-slash"></i> Ban Autore
              </button>
            </form>

            <form action="${pageContext.request.contextPath}/ReportServlet" method="get">
              <input type="hidden" name="action" value="deleteReport">

              <input type="hidden" name="id" value="${report.id}">

              <button type="submit" class="btn btn-ignore">
                <i class="fas fa-check"></i> Ignora
              </button>
            </form>

            <a href="${pageContext.request.contextPath}/PostServlet#post-${report.postId}" target="_blank" class="btn btn-view">
              <i class="fas fa-external-link-alt"></i> Vedi Post
            </a>

          </div>
        </div>
      </div>
    </c:forEach>

    <c:if test="${empty reports}">
      <div class="empty-state">
        <i class="fas fa-check-circle"></i>
        <h3>Tutto tranquillo!</h3>
        <p>Non ci sono nuove segnalazioni da gestire.</p>
      </div>
    </c:if>

  </div>
</div>

<jsp:include page="../includes/footer.jsp" />

<script>
  function toggleReport(id) {
    const details = document.getElementById('details-' + id);
    const card = document.getElementById('card-' + id);
    const icon = card.querySelector('.report-expand-icon i');

    if (details.style.maxHeight) {
      // Chiudi
      details.style.maxHeight = null;
      details.style.opacity = '0';
      details.style.padding = '0 25px';
      icon.classList.remove('fa-chevron-up');
      icon.classList.add('fa-chevron-down');
      card.classList.remove('active');
    } else {
      // Apri
      details.style.maxHeight = details.scrollHeight + "px";
      details.style.opacity = '1';
      details.style.padding = '25px';
      icon.classList.remove('fa-chevron-down');
      icon.classList.add('fa-chevron-up');
      card.classList.add('active');
    }
  }
</script>

</body>
</html>