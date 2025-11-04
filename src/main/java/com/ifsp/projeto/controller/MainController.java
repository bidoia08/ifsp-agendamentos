package com.ifsp.projeto.controller;

import com.ifsp.projeto.model.Agendamento;
import com.ifsp.projeto.model.Usuario;
import com.ifsp.projeto.repository.AgendamentoRepository;
import com.ifsp.projeto.repository.UsuarioRepository;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private AgendamentoRepository agendamentoRepo;

    // === LOGIN ===
    @GetMapping("/")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String nome,
                    @RequestParam String senha,
                    Model model,
                    HttpSession session) {

        var user = usuarioRepo.findByNomeUsuarioAndSenha(nome, senha);

        if (user.isPresent()) {
            // ✅ Guarda o usuário logado na sessão
            session.setAttribute("usuarioLogado", user.get());
            return "redirect:/principal";
        }

        model.addAttribute("erro", "Usuário ou senha inválidos!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }



    // === CADASTRO ===
    @GetMapping("/cadastro")
    public String cadastroForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        // opcional: checar se nomeUsuario já existe
        if (usuarioRepo.findByNomeUsuario(usuario.getNomeUsuario()).isPresent()) {
            redirectAttributes.addFlashAttribute("erro", "Nome de usuário já existe.");
            return "redirect:/cadastro";
        }
        usuarioRepo.save(usuario);
        redirectAttributes.addFlashAttribute("mensagem", "Cadastro realizado com sucesso! Faça login.");
        return "redirect:/";
    }


    // === PRINCIPAL ===
    @GetMapping("/principal")
    public String principal() {
        return "principal";
    }

    // === AGENDAR ===
    @GetMapping("/agendar")
    public String agendarForm(Model model) {
        model.addAttribute("agendamento", new Agendamento());
        return "agendar";
    }

    @PostMapping("/agendar")
    public String salvarAgendamento(@ModelAttribute Agendamento agendamento,
                                Model model,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado == null) {
            redirectAttributes.addFlashAttribute("erro", "Você precisa estar logado!");
            return "redirect:/";
        }

        LocalDate hoje = LocalDate.now();

        // ⚙️ Define o responsável automaticamente
        agendamento.setResponsavel(usuarioLogado.getNomeUsuario());
        


        // Validação: data passada
        if (agendamento.getData().isBefore(hoje)) {
            model.addAttribute("agendamento", agendamento);
            model.addAttribute("erro", "⚠ Não é possível agendar uma data passada!");
            return "agendar";
        }

        // Validação: horário início < término
        if (agendamento.getHoraInicio().isAfter(agendamento.getHoraFim()) ||
            agendamento.getHoraInicio().equals(agendamento.getHoraFim())) {
            model.addAttribute("agendamento", agendamento);
            model.addAttribute("erro", "⚠ O horário de início deve ser anterior ao horário de término!");
            return "agendar";
        }

        // Verifica conflitos — passando o id para ignorar o próprio agendamento se for edição
        boolean existeConflito = agendamentoRepo.verificarConflitos(
                agendamento.getSala(),
                agendamento.getData(),
                agendamento.getHoraInicio(),
                agendamento.getHoraFim(),
                agendamento.getId()
        );

        if (existeConflito) {
            model.addAttribute("agendamento", agendamento);
            model.addAttribute("erro", "⚠ Já existe um agendamento para esta sala e horário!");
            return "agendar";
        }

        // Associa o agendamento ao usuário logado (somente se for novo)
        
        if (usuarioLogado != null && agendamento.getId() == null) {
            agendamento.setUsuario(usuarioLogado);
        }

        // Salva (cria ou atualiza conforme presença do id)
        agendamentoRepo.save(agendamento);

        redirectAttributes.addFlashAttribute("mensagem", "✅ Agendamento realizado com sucesso!");
        return "redirect:/ver-agendamentos";
    }

    // === VER AGENDAMENTOS ===
    @GetMapping("/ver-agendamentos")
    public String verAgendamentos(@RequestParam(required = false) String data,
                                HttpSession session, Model model) {

        List<Agendamento> agendamentos;
        LocalDate hoje = LocalDate.now();

        if (data != null) {
            LocalDate dataFiltro = LocalDate.parse(data);
            agendamentos = agendamentoRepo.findByData(dataFiltro)
                    .stream()
                    .filter(a -> !a.getData().isBefore(hoje)) // ✅ Exclui datas passadas
                    .toList();
            model.addAttribute("filtroData", dataFiltro);
        } else {
            agendamentos = agendamentoRepo.findAll()
                    .stream()
                    .filter(a -> !a.getData().isBefore(hoje)) // ✅ Exclui datas passadas
                    .toList();
        }

        model.addAttribute("agendamentos", agendamentos);

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        model.addAttribute("usuarioLogado", usuarioLogado);

        return "ver-agendamentos";
    }

    @GetMapping("/historico")
    public String verHistorico(HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        LocalDate hoje = LocalDate.now();

        // Busca apenas os agendamentos anteriores a hoje
        List<Agendamento> historico = agendamentoRepo.findAll()
                .stream()
                .filter(a -> a.getData().isBefore(hoje))
                .sorted((a1, a2) -> a2.getData().compareTo(a1.getData())) // mais recentes primeiro
                .toList();

        model.addAttribute("historico", historico);
        model.addAttribute("usuarioLogado", usuarioLogado);

        return "historico";
    }


    // === EDITAR ===
    @GetMapping("/editar/{id}")
    public String editarAgendamento(@PathVariable Long id,
                                Model model,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {

        var ag = agendamentoRepo.findById(id).orElse(null);
        if (ag == null) {
            redirectAttributes.addFlashAttribute("erro", "Agendamento não encontrado.");
            return "redirect:/ver-agendamentos";
        }

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado == null) {
            redirectAttributes.addFlashAttribute("erro", "Você precisa estar logado!");
            return "redirect:/";
        }

        // Se não for admin e não for o criador → bloqueia
        if (!"ADMIN".equalsIgnoreCase(usuarioLogado.getTipo()) &&
            (ag.getUsuario() == null || !ag.getUsuario().getId().equals(usuarioLogado.getId()))) {
            redirectAttributes.addFlashAttribute("erro", "Você só pode editar seus próprios agendamentos!");
            return "redirect:/ver-agendamentos";
        }

        model.addAttribute("agendamento", ag);
        return "agendar";
    }


    @GetMapping("/excluir/{id}")
    public String excluirAgendamento(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
    Usuario logado = (Usuario) session.getAttribute("usuarioLogado");

    if (logado == null) {
        redirectAttributes.addFlashAttribute("erro", "Você precisa estar logado!");
        return "redirect:/";
    }

    Optional<Agendamento> optAg = agendamentoRepo.findById(id);
    if (optAg.isEmpty()) {
        redirectAttributes.addFlashAttribute("erro", "Agendamento não encontrado.");
        return "redirect:/ver-agendamentos";
    }

    Agendamento agendamento = optAg.get();

    // Se for admin, pode excluir qualquer um
    if ("ADMIN".equalsIgnoreCase(logado.getTipo())) {
        agendamentoRepo.deleteById(id);
        redirectAttributes.addFlashAttribute("mensagem", "🗑️ Agendamento excluído com sucesso!");
        return "redirect:/ver-agendamentos";
    }

    // Se for user, só pode excluir o próprio
    if (agendamento.getUsuario() != null && agendamento.getUsuario().getId().equals(logado.getId())) {
        agendamentoRepo.deleteById(id);
        redirectAttributes.addFlashAttribute("mensagem", "🗑️ Seu agendamento foi excluído com sucesso!");
    } else {
        redirectAttributes.addFlashAttribute("erro", "⚠️ Você só pode excluir seus próprios agendamentos!");
    }

    return "redirect:/ver-agendamentos";
}


    @GetMapping("/recuperar-senha")
    public String mostrarRecuperarSenhaForm() {
        return "recuperar-senha";
    }
    
    @PostMapping("/recuperar-senha")
    public String recuperarSenha(@RequestParam String nomeUsuario,
                                 @RequestParam String palavraSeguranca,
                                 Model model) {
        Usuario usuario = usuarioRepo.findByNomeUsuarioAndPalavraSeguranca(nomeUsuario, palavraSeguranca);
    
        if (usuario != null) {
            model.addAttribute("mensagem", "Sua senha é: " + usuario.getSenha());
        } else {
            model.addAttribute("erro", "Usuário ou palavra de segurança incorretos.");
        }
        return "recuperar-senha";
    }

    // método a colar/substituir
    @GetMapping("/calendario")
    public String calendario(
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "ano", required = false) Integer ano,
            Model model) {

        LocalDate hoje = LocalDate.now();

        // Se não vierem parâmetros, usa o mês e ano atuais
        if (mes == null) mes = hoje.getMonthValue();
        if (ano == null) ano = hoje.getYear();

        LocalDate primeiroDia = LocalDate.of(ano, mes, 1);
        int ultimoDia = primeiroDia.lengthOfMonth();
        int primeiroDiaSemana = primeiroDia.getDayOfWeek().getValue() % 7;

        // Recupera agendamentos
        List<Agendamento> agendamentos = agendamentoRepo.findAll();
        Set<LocalDate> datasAgendadas = agendamentos.stream()
                .map(Agendamento::getData)
                .collect(Collectors.toSet());

        // Monta estrutura de semanas
        List<List<Map<String, Object>>> weeks = new ArrayList<>();
        List<Map<String, Object>> week = new ArrayList<>();

        for (int i = 0; i < primeiroDiaSemana; i++) {
            week.add(Collections.emptyMap());
        }

        for (int day = 1; day <= ultimoDia; day++) {
            LocalDate d = LocalDate.of(ano, mes, day);
            Map<String, Object> cell = new HashMap<>();
            cell.put("day", day);
            cell.put("dataStr", d.toString());
            cell.put("agendado", datasAgendadas.contains(d));
            cell.put("hoje", d.equals(hoje));
            week.add(cell);

            if (week.size() == 7) {
                weeks.add(week);
                week = new ArrayList<>();
            }
        }

        if (!week.isEmpty()) {
            while (week.size() < 7) {
                week.add(Collections.emptyMap());
            }
            weeks.add(week);
        }

        // Nome do mês atual
        String mesNome = primeiroDia.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));

        // Calcular mês anterior e próximo
        LocalDate anterior = primeiroDia.minusMonths(1);
        LocalDate proximo = primeiroDia.plusMonths(1);

        // Enviar tudo para o modelo
        model.addAttribute("weeks", weeks);
        model.addAttribute("mesNome", mesNome);
        model.addAttribute("ano", ano);
        model.addAttribute("mes", mes);
        model.addAttribute("mesAnterior", anterior.getMonthValue());
        model.addAttribute("anoAnterior", anterior.getYear());
        model.addAttribute("mesProximo", proximo.getMonthValue());
        model.addAttribute("anoProximo", proximo.getYear());

        return "calendario";
    }

}